#include <jni.h>
#include <stdarg.h>
#include <stdio.h>

#include "bfd.h"
#include "dis-asm.h"

#define RESULTSIZE 8192

/* Really we should somehow keep it in sync with the defs in the SubArchOption Java Class */
#define FLAG_BigEndian 1

/* Structure to be passed around for us to print into a buffer via an fprintf like function */
struct ourstringstream {
  char* buffer;
  unsigned int len;     /* Length of the buffer */
  unsigned int towrite; /* Index into buffer to place that can be written */
};

/* Returns number of characters printed.  Note that stream is really a pointer to struct ourstringstream */
int ourstringprintf(FILE* stream, const char* format, ...) {
  struct ourstringstream* sstream=(struct ourstringstream*)stream;
  int result;
  va_list val;

  va_start(val, format);
  if (sstream->towrite < sstream->len) {
    result=vsnprintf(sstream->buffer + sstream->towrite, sstream->len-sstream->towrite, format, val);
  } else {
    result=0;
  }
  va_end(val);

  sstream->towrite+=result;

  return result;
}

/* Based very loosely on the code from objdump */

JNIEXPORT jstring JNICALL Java_org_treblig_pocketdisassembler_BinUtilsGlue_doDisassemble( JNIEnv* env,
			jobject thisbug,
			jstring jarchstr, jstring jsubarchstr, jstring joptionsstr, jint jflags,
			jbyteArray jhexdata ) {
  bfd abfd;
  struct disassemble_info dinfo;
  disassembler_ftype thedis;
  struct ourstringstream oss;

  jboolean iscopy;
  const char *archstr,*subarchstr,*optionsstr;
  char result[RESULTSIZE];
  int nprocessed, totalprocessed;

  char *hexdata;

  archstr = (*env)->GetStringUTFChars(env, jarchstr, &iscopy);
  subarchstr = (*env)->GetStringUTFChars(env, jsubarchstr, &iscopy);
  optionsstr = (*env)->GetStringUTFChars(env, joptionsstr, &iscopy);

  init_disassemble_info(&dinfo, stderr, (fprintf_ftype) fprintf);
  /* TODO: abfd should come from bfd_openr_iovec???  with dummy funcs */
  abfd.arch_info = bfd_scan_arch(subarchstr);
  if (!abfd.arch_info) {
    sprintf(result,"Could not find architecture '%s'", subarchstr);
    goto errexit;
  };

  bfd_find_target("binary", &abfd);
  dinfo.arch= bfd_get_arch (&abfd);
  thedis = disassembler (&abfd);

  if (!thedis) {
    sprintf(result,"Could not get disassembler from bfd");
    goto errexit;
  };

  snprintf(result, RESULTSIZE, "%s/%s/%s", archstr, subarchstr, optionsstr);

  oss.buffer=result;
  oss.towrite=0;
  oss.len=RESULTSIZE;

  dinfo.fprintf_func = (fprintf_ftype)ourstringprintf;
  dinfo.stream=(FILE*)&oss;
  dinfo.endian = (jflags & FLAG_BigEndian)?BFD_ENDIAN_BIG:BFD_ENDIAN_LITTLE;
  dinfo.symtab = NULL;
  dinfo.symtab_size = 0;
  dinfo.symbols=NULL;
  dinfo.section=NULL;
  dinfo.disassembler_options=optionsstr;

  disassemble_init_for_target(&dinfo);
  dinfo.buffer_vma = 0;
  dinfo.buffer_length = (*env)->GetArrayLength(env, jhexdata);
  hexdata = (*env)->GetByteArrayElements(env, jhexdata, &iscopy);
  dinfo.buffer = hexdata;

  totalprocessed=0;
  
  if (dinfo.buffer_length) {
    do {
      nprocessed=(*thedis)(totalprocessed, &dinfo);
      totalprocessed+=nprocessed;
      ourstringprintf((FILE*)&oss, "\n");
    } while ((nprocessed>0) && (totalprocessed < dinfo.buffer_length));
  }

errexit:
  (*env)->ReleaseStringUTFChars(env, jarchstr, archstr);
  (*env)->ReleaseStringUTFChars(env, jsubarchstr, subarchstr);
  (*env)->ReleaseStringUTFChars(env, joptionsstr, optionsstr);
  (*env)->ReleaseByteArrayElements(env, jhexdata, (jbyte*)hexdata, 0);

  return (*env)->NewStringUTF(env,result);

}
