#!/usr/sh
echo "test read"
LD_PRELOAD=../lib/lib440lib.so ../tools/440read ./Makefile
echo "***********************************"
echo "test cat"
LD_PRELOAD=../lib/lib440lib.so ../tools/440cat ./Makefile
echo "***********************************"
echo ">> LD_PRELOAD=../lib/lib440lib.so ./single_copy Makefile testMakefile"
cc single_copy.c -o single_copy
rm testMakefile test100 testSeek
LD_PRELOAD=../lib/lib440lib.so ./single_copy Makefile testMakefile
diff Makefile testMakefile
echo "***********************************"
echo ">> LD_PRELOAD=../lib/lib440lib.so ./single_copy Makefile testMakefile100Bytes 100"
LD_PRELOAD=../lib/lib440lib.so ./single_copy Makefile test100 100

echo "***********************************"
echo ">> LD_PRELOAD=../lib/lib440lib.so ./single_copy Makefile testMakefileLseek 100 100"
LD_PRELOAD=../lib/lib440lib.so ./single_copy Makefile testSeek 100 100

echo "***********************************"
cc errno_test1.c -o errno_test1
echo ">> LD_PRELOAD=../lib/lib440lib.so ./errno_test1 Makefile test"
LD_PRELOAD=../lib/lib440lib.so ./errno_test1 Makefile test
