PocketDisassembler  - Version 0.0.4

For info on where to find the latest version look at http://www.treblig.orgi/pocketdisassembler

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
  Allow cr in input

  Custom keyboard
    maybe set popupKeyboard extra ?
    Android nethack has a keyboard http://code.google.com/p/nethack-android/source/browse/trunk/nethack-3.4.3/sys/android/NetHackApp/src/com/nethackff/
       defines it's own keyboard but also has an XML description
         which it reads in using new Keyboard(app, R.xml.name) (Keyboard from android.inputmethodservice.Keyboard)
also uses a KeyboardView (from R.layout.input)
         it's adding that in with a layout.addView(virtualKeyboard.virtualKeyboardView)
    http://stackoverflow.com/questions/3938523/how-does-an-android-app-load-a-keyboard

10) Building
  Note these instructions describe a build on Ubuntu 12.10 on x86
  builting ARM binaries.  Other combinations untried.

  a) To build binutils:
   You need to do a binutils build/install 1st - the jni build needs the headers

     or for ndk-r8b:
       make a standalone ndk setup (only need to do it once); e.g. at the top of your ndk installation do:
       build/tools/make-standalone-toolchain.sh --platform=android-8 --install-dir=$PWD/standalone-tc-arm-android8

       export NDKTCPATH=/discs/more/android/dev/android-ndk-r8b/standalone-tc-arm-android8
       export PATH=$PATH:$NDKTCPATH/bin

     # Now you want to be in pocketdisassembler/binutils/build
     CC=$NDKTCPATH/bin/arm-linux-androideabi-gcc CXX=$NDKTCPATH/bin/arm-linux-androideabi-g++ ../binutils.gitcheckout/configure --prefix=$PWD/../result --host=arm-linux-androideabi --enable-targets=alpha-unknown-linux-elf,arm-linux-gnueabi,hppa-linux-elf,i686-linux-gnueabi,ia64-linux-elf,m68-linux-elf,mips-linux-elf,ppc-linux-gnueabi,s390-linux-gnueabi,sparc-linux-elf,aarch64-linux-gnueabi --enable-shared --disable-nls

     make -j 8
     make install
     # and you should find you have a set of binaries in binutils/result

  b) To build the jni glue
     At the top level of this tree run:
     ndk-build   (having added the NDK to your path - note this is the ndk, not the ndk toolchain!)
Note that this nukes any builds you have of the binutils, so you have to copy that back into
the libs directory (see below).
     # Now you should have libs/armeabi/libbinutilsglue.so

  c) Copy the binutils binaries in
   cp binutils/result/lib/libopcodes-*.so libs/armeabi/
   cp binutils/result/lib/libbfd-*.so libs/armeabi/

  d) Fix up BinUtilsGlue.java with the name of the libopcode and libbfd you've got
     (I'm not quite sure how to avoid this; certainly just putting the libbfd.so and libopcodes.so
      in doesn't work because the name of the libbfd is in the NEEDED header in libopcode.)

11) Changes
  For 0.0.4
    Disabled keyboard on hex input; it was getting in the way until I figure out a full soft keyboard
      - Not convinced paste/select is necessarily happy with it though.
    Disabled autocompletion
    Moved to git (as of 2012-12-26) binutils
    Enabled aarch64 (That's ARM 64-bit)
    Move the disassemble button between the hex entry and the number keys; I was finding that a selection
      toast from the hex entry would get in the way of a number key.
