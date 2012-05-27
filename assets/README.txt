PocketDisassembler  - Version 0.0.2

0) Copying
  Welcome to PocketDisassembler - the front end was written by
Dave Gilbert <dave@treblig.org> and uses the Binutils libbfd and libopcodes
from the GNU binutils package.

  Both PocketDisassembler and binutils are licensed under GPLv3 - you should
find a copy of GPLv3 in the distribution.

  The source is available on http://www.treblig.org

1) Using
  Type in the hex to disassemble, choose the architecture and varient,
select any options it might show you and hit disassemble - that's it.

  The hex entry takes space separated hex entries; if you enter a series
of bytes - say 01 02 03 04 - they'll end up in memory with 01 at the 1st byte,
02 at the 2nd etc (irrespective of any endian settings); this works
nicely for entering x86 hex.  If you enter more than byte quantities - e.g.
0102 0304 then the behaviour depends on the endianness. The first group
always goes into memory at the lowest address, so 0102 goes before 0304
irrespective of endianness.  For little endian the layout would be
02 in 1st byte, 01 in 2nd, 04 in 3rd, 03 in 4th for big endian it would be 
01 in 1st byte, 02 in 2nd, 03 in 3rd, 04 in 4th byte.  So for little endian
32bit instructions just enter the instruction as a whole word like
ef000001 - this works well for architectures like ARM.

2) Supported architectures
  I should be able to build this to work with any architecutre that binutils
supports; I've only enabled a few at the moment, if there is one you want
that's missing, please check if binutils supports it, and if it does
then just mail me to ask.

3) Bugs
  I'm sure there are some - in particular I've only actually tried
a few of the architecture/subarchitecture combos, and haven't checked
them against the manuals for those architectures - if you find a bug
just mail me at dave@treblig.org   - see the TODO list below though.

4) TODO
  Update readme
  Add other archs/subarchs
     Need to get subarchs to work
       mips:ia6r2 not happy

  Merge subArchOptions into subArchDef and remove the index's and turn them into references

  Allow cr in input
  Icon
  Custom keyboard
    maybe set popupKeyboard extra ?
    Android nethack has a keyboard http://code.google.com/p/nethack-android/source/browse/trunk/nethack-3.4.3/sys/android/NetHackApp/src/com/nethackff/
       defines it's own keyboard but also has an XML description

10) Building
To build the jni glue run    ndk-build (from wherever your ndk installation is) at the top level.
Note that this nukes any builds you have of the binutils, so you have to copy that back into
the libs directory (see below).

To build binutils:

export PATH=$PATH:/discs/more/android/dev/ndk-toolchain-r8-arm/bin
CC=/discs/more/android/dev/ndk-toolchain-r8-arm/bin/arm-linux-androideabi-gcc CXX=/discs/more/android/dev/ndk-toolchain-r8-arm/bin/arm-linux-androideabi-g++ ../binutils-2.22/configure --prefix=/home/dg/workspace/PocketDisassembler/binutils/result --host=arm-linux-androideabi --enable-targets=alpha-unknown-linux-elf,arm-linux-gnueabi,hppa-linux-elf,i686-linux-gnueabi,ia64-linux-elf,m68-linux-elf,mips-linux-elf,ppc-linux-gnueabi,s390-linux-gnueabi,sparc-linux-elf --enable-shared --disable-nls
make -j 8


cp binutils/build/opcodes/.libs/libopcodes-2.22.so libs/armeabi/
cp binutils/build/bfd/.libs/libbfd-2.22.so libs/armeabi/

so workspace/PocketDisassembler/libs/armeabi/libopcodes-2.22.so

patch to libiberty/getpagesize.c - don't define on android
                   setproctitle - change to prctl (PR_SET_NAME, (unsigned long)name, 0, 0, 0);
         bfd/archive.c - cast &status.st_mtime to time_t*
         binutils/bucomm.c  - declare mkdtemp (missing decleration on android)
         binutils/readelf.c not to use minor/major
         ld/plugin.c not to use minor/major


