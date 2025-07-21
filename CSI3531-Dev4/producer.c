/*
How to Run:

1. Compile both programs using a C compiler (e.g., Visual Studio, gcc on Windows with MinGW):

   For example (with gcc):
     gcc producer.c -o producer
     gcc consumer.c -o consumer

2. Open two separate command prompts (Terminal 1 and Terminal 2):

   - In Terminal 1, run the producer and provide the number of Catalan numbers to generate:
       ./producer 5

   - In Terminal 2, run the consumer to read and print the Catalan numbers:
       ./consumer

Note:
- The producer creates a shared memory region and writes the sequence.
- The consumer opens the same shared memory and reads the data.
- Make sure to run the consumer *after* the producer to ensure the data is ready.
*/
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>

// Compute Catalan number using recursive formula
unsigned long long computeCatalan(int n) {
    unsigned long long catalan = 1;
    for (int i = 0; i < n; i++) {
        catalan = catalan * 2 * (2 * i + 1) / (i + 2);
    }
    return catalan;
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Usage: producer <number_of_terms>\n");
        return 1;
    }

    int n = atoi(argv[1]);
    size_t dataSize = (n + 1) * sizeof(unsigned long long);  // +1 to store 'n' itself

    HANDLE hMapFile = CreateFileMapping(
        INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0,
        (DWORD)dataSize, "Local\\CatalanSharedMemory"
    );

    if (hMapFile == NULL) {
        printf("Error: Could not create mapping (%lu).\n", GetLastError());
        return 1;
    }

    void* pBuf = MapViewOfFile(hMapFile, FILE_MAP_ALL_ACCESS, 0, 0, dataSize);
    if (pBuf == NULL) {
        printf("Error: Could not map view (%lu).\n", GetLastError());
        CloseHandle(hMapFile);
        return 1;
    }

    unsigned long long* data = (unsigned long long*)pBuf;

    data[0] = n;  // First value is the count of Catalan numbers

    for (int i = 1; i <= n; i++) {
        data[i] = computeCatalan(i);  // Store C_i at index i
    }

    printf("Producer: Wrote C_1 to C_%d to shared memory.\n", n);
    printf("Press Enter to exit the producer...\n");
    getchar();

    UnmapViewOfFile(pBuf);
    CloseHandle(hMapFile);
    return 0;
}
