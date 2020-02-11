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
    if (fd1 < 0) err(1, 0);
    int fd2 = open(file2, O_WRONLY | O_CREAT | O_TRUNC, S_IRWXU);
    if (fd2 < 0) err(1, 0);

    struct stat file_stat;
    rv = __xstat(0, file1, &file_stat);
    if (rv < 0) err(1, 0);
    int size = file_stat.st_size;
    rv = lseek(fd1, start, SEEK_CUR);
    if (rv < 0) err(1, 0);
    if (n == -1) n = size;
    char *buf = (char *) malloc(size+10);
    printf("size = %d\tn = %d\tstart=%lu\n", size, n, start);
    rv = read(fd1, buf, n);
    printf("read %d bytes\n", rv);
    if (rv < 0) err(1, 0);
    rv = write(fd2, buf, rv);
    printf("write %d bytes\n", rv);
    if (rv < 0) err(1, 0);
    rv = close(fd1);
    if (rv < 0) err(1, 0);
    rv = close(fd2);
    if (rv < 0) err(1, 0);
    free(buf);
}
