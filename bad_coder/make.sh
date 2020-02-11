#!/bin/sh
export CLASSPATH=$PWD:$PWD/../lib
export proxyport15440=167900
export pin5440=999999999
make
# tar cvzf ../mysolution.tgz Makefile Proxy.java Server.java