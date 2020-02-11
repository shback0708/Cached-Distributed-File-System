/* Sample skeleton for proxy */

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

class Proxy {
	private static class FileHandler implements FileHandling {
		// Store the mapping between file descriptor and the file.
		private final HashMap<Integer, RandomAccessFile> fd2RAF;
		// Store the mapping between file descriptor and the file.
		private final HashMap<Integer, File> fd2File;
		// Store the files that have the write access.
		private final HashSet<Integer> openWriteFiles;
		// Cumulative file descriptor.
		private int fdUsed;
		// file descriptor limit.
		private final int fdLimit;

		public FileHandler() {
			fdUsed = 1024;
			fdLimit = 9999;
			openWriteFiles = new HashSet<Integer>();
			fd2RAF = new HashMap<Integer, RandomAccessFile>();
			fd2File = new HashMap<Integer, File>();
		}

		public int open(String path, OpenOption o) {
			System.err.println("open " + path + " option=" + String.valueOf(o));
			boolean write = false;
			boolean isDirectory = false;
			try {
				File f = new File(path);
				RandomAccessFile file = null;
				switch (o) {
					case CREATE: // Open a file for read/write, create if it does not exist
						if (f.isDirectory()) {
							return Errors.EISDIR;
						}
						file = new RandomAccessFile(f, "rw");
						write = true;
						break;

					case CREATE_NEW: // Create new file for read/write, returning error if
										// it already exists
						if (f.exists()) {
							return Errors.EEXIST;
						}
						if (f.isDirectory()) {
							return Errors.EISDIR;
						}
						file = new RandomAccessFile(f, "rw");
						write = true;
						break;

					case READ: // Open existing file or directory for read only
						if (!f.exists()) {
							return Errors.ENOENT;
						}
						if (f.isDirectory()) {
							isDirectory = true;
							break;
						}
						file = new RandomAccessFile(f, "r");
						break;

					case WRITE: // Open existing fie for read/write
						if (!f.exists()) {
							return Errors.ENOENT;
						}
						if (f.isDirectory()) {
							return Errors.EISDIR;
						}
						file = new RandomAccessFile(f, "rw");
						write = true;
						break;

					default:
						return Errors.EINVAL;
				}

				if (fdUsed >= fdLimit) {
					return Errors.EMFILE;
				}
				++fdUsed;
				System.err.println("open: fd=" + fdUsed);
				fd2RAF.put(fdUsed, file);
				fd2File.put(fdUsed, f);
				if (write) {
					openWriteFiles.add(fdUsed);
				}
				return fdUsed;

			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				if (e instanceof AccessDeniedException) {
					return Errors.EBADF;
				} else { 
					return Errors.ENOSYS;
				}
			}
		}

		public int close(int fd) {
			System.err.println("close fd = " + fd);
			if (!fd2RAF.containsKey(fd)) {
				return Errors.EBADF;
			}
			RandomAccessFile file = fd2RAF.get(fd);
			File f = fd2File.get(fd);
			if (f.isDirectory()) {
				fd2RAF.remove(fd);
				fd2File.remove(fd);
				if (openWriteFiles.contains(fd)) {
					openWriteFiles.remove(fd);
				}
			} else {
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace(System.err);
					System.err.println(e);
				}
			
				fd2RAF.remove(fd);
				fd2File.remove(fd);
				if (openWriteFiles.contains(fd)) {
					openWriteFiles.remove(fd);
				}
			}
			return 0;
		}

		public long write(int fd, byte[] buf) {
			System.err.println("write fd = " + fd);
			if (!fd2RAF.containsKey(fd) || !openWriteFiles.contains(fd)) {
				return Errors.EBADF;
			}
			File f = fd2File.get(fd);
			if (f.isDirectory()) {
				return Errors.EISDIR;
			}

			if (!f.canWrite()) {
				return Errors.EPERM;
			}
			RandomAccessFile file = fd2RAF.get(fd);
			try {
				file.write(buf);
			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				return Errors.EBADF;

			} catch (NullPointerException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				return Errors.EINVAL;
			}
			return Long.valueOf(buf.length);
		}

		public long read(int fd, byte[] buf) {
			System.err.println("read fd = " + fd);
			if (!fd2RAF.containsKey(fd)) {
				return Errors.EBADF;
			}
			File f = fd2File.get(fd);
			if (f.isDirectory()) {
				return Errors.EISDIR;
			}
			if (!f.canRead()) {
				return Errors.EPERM;
			}
			RandomAccessFile file = fd2RAF.get(fd);
			try {
				int ret = file.read(buf);
				if (ret == -1)
					return 0L;
				else
					return (long) ret;

			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				return Errors.EBADF;

			} catch (NullPointerException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				return Errors.EINVAL;
			}
		}

		public long lseek(int fd, long pos, LseekOption o) {
			System.err.println("lseek fd = " + fd + ", pos=" + pos);
			if (!fd2RAF.containsKey(fd)) {
				return Errors.EBADF;
			}
			File f = fd2File.get(fd);
			if (f.isDirectory()) {
				return Errors.EISDIR;
			}
			RandomAccessFile file = fd2RAF.get(fd);
			try {
				long currentPosition = file.getFilePointer();
				switch (o) {
				case FROM_CURRENT:
					file.seek(currentPosition + pos);
					currentPosition = file.getFilePointer();
					break;

				case FROM_END:
					long endPostion = file.length();
					file.seek(endPostion + pos);
					currentPosition = file.getFilePointer();
					break;

				case FROM_START:
					file.seek(pos);
					currentPosition = file.getFilePointer();
					break;

				default:
					return Errors.EINVAL;
				}
				return currentPosition;

			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				return Errors.EINVAL;
			}
		}

		public int unlink(String path) {
			System.err.println("unlink");
			if (path == null) {
				return Errors.ENOENT;
			}
			File f = new File(path);
			if (!f.exists()) {
				return Errors.ENOENT;
			}
			if (f.isDirectory()) {
				return Errors.EISDIR;
			}
			try {
				Files.delete(f.toPath());
			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.err.println(e);
				return Errors.EBUSY;
			}
			return 0;
		}

		public void clientdone() {
			System.err.println("clientdone");
			fdUsed = 1024;
			fd2RAF.clear();
			openWriteFiles.clear();
			fd2File.clear();
			return;
		}
	}

	private static class FileHandlingFactory implements FileHandlingMaking {
		public FileHandling newclient() {
			return new FileHandler();
		}
	}

	public static void main(String[] args) throws IOException {
		System.err.println("Hello World");
		RPCreceiver runner = new RPCreceiver(new FileHandlingFactory());
		Thread t = new Thread(runner);
		t.start();
	}
}
