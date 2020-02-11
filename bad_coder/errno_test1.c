#include <pthread.h>
#include <unistd.h>  
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <err.h>

int main(int argc, char **argv){
    int n, rv;
    char file1[10];
    char file2[10];
    strcpy(file1, argv[1]);
    strcpy(file2, argv[2]);
    int opt;
    off_t start=0;
    if (argc >= 4) { 
        n = atoi(argv[3]);
    }
    else {
        n = -1;
    }

    if (argc >= 5){
        start = atoi(argv[4]);
    }
    // rv = unlink(file2);
    // if (rv < 0) err(1, 0);
    int fd1 = open(file1, O_RDONLY);
    rv = close(fd1);
    if (rv < 0) err(1, 0);
    // if (fd1 < 0) err(1, 0);
    // rv = read(fd1, "sadsa", 5);
}
