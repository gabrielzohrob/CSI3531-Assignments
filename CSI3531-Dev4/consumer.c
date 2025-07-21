#include <windows.h>
#include <stdio.h>

int main() {
    // Open the existing shared memory
    HANDLE hMapFile = OpenFileMapping(
        FILE_MAP_READ, FALSE, "Local\\CatalanSharedMemory"
    );

    if (hMapFile == NULL) {
        printf("Error: Could not open file mapping object (%lu).\n", GetLastError());
        return 1;
    }

    // Map initial memory just to read the count (n)
    void* pBuf = MapViewOfFile(hMapFile, FILE_MAP_READ, 0, 0, sizeof(unsigned long long));
    if (pBuf == NULL) {
        printf("Error: Could not map initial view (%lu).\n", GetLastError());
        CloseHandle(hMapFile);
        return 1;
    }

    unsigned long long* data = (unsigned long long*)pBuf;
    unsigned long long n = data[0];  // Read the count
    UnmapViewOfFile(pBuf);           // Unmap short view

    // Now remap full memory including all Catalan numbers
    size_t dataSize = (n + 1) * sizeof(unsigned long long);
    pBuf = MapViewOfFile(hMapFile, FILE_MAP_READ, 0, 0, dataSize);
    if (pBuf == NULL) {
        printf("Error: Could not map full view (%lu).\n", GetLastError());
        CloseHandle(hMapFile);
        return 1;
    }

    data = (unsigned long long*)pBuf;

    printf("Consumer: Catalan numbers from shared memory:\n");
    for (int i = 1; i <= n; i++) {
        printf("C_%d = %llu\n", i, data[i]);  // Start from C_1
    }

    UnmapViewOfFile(pBuf);
    CloseHandle(hMapFile);
    return 0;
}
