.lib "zlibwapi.dll"

; ----------------------------------------------------------------------
; -- ZlibWapi.decls
; -- 
; -- DLL functions for accessing & manipulating ZIP files.
; -- Use the helper functions in "Blitz_File_ZipApi.bb" where 
; -- applicable.
; -- 
; -- Version       : 1.0.0
; -- Homepage      : http://www.sodaware.net/dev/blitz/libs/Blitz.ZipApi/
; -- Documentation : http://docs.sodaware.net/blitz.zipapi/
; -- Zlib Homepage : http://www.zlib.net/
; ----------------------------------------------------------------------


; --------------------------------------------------
; -- See the documentation for advice on which 
; -- functions to use. Most have been wrapped with
; -- helper functions to make life easier.
; -- For example, adding a file to a ZIP can be 
; -- done with ZipApi_AddFile instead of using 
; -- ZlibWapi_ZipOpenNewFileInZip.
; --------------------------------------------------

; --------------------------------------------------
; -- API Functions
; --------------------------------------------------

; -- Library Information

; ZlibWapi_zError
; ZlibWapi_ZlibVersion

; -- Utility Functions

; ZlibWapi_CompressBound

; -- Compression / UnCompression functions

; ZlibWapi_Compress 
; ZlibWapi_UnCompress

; -- Reading from ZIP files

; ZlibWapi_UnzOpen
; ZlibWapi_UnzClose
; ZlibWapi_UnzGetGlobalInfo
; ZlibWapi_UnzGetGlobalComment
; ZlibWapi_UnzLocateFile
; ZlibWapi_UnzGoToFirstFile
; ZlibWapi_UnzGoToNextFile
; ZlibWapi_UnzGetCurrentFileInfo
; ZlibWapi_UnzOpenCurrentFile
; ZlibWapi_UnzReadCurrentFile
; ZlibWapi_UnzCloseCurrentFile

; -- Writing to ZIP files

; ZlibWapi_ZipOpen
; ZlibWapi_ZipClose
; ZlibWapi_ZipOpenNewFileInZip
; ZlibWapi_ZipWriteFileInZip
; ZlibWapi_ZipCloseFileInZip

; ----------------------------------------
; -- Library Information
; ----------------------------------------

;;; <summary>Gets a string indentifying an error with the code "errorCode".</summary>
;;; <param name="errorCode">The error code to lookup.</param>
;;; <returns>Error code string, or NULL if not found.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_zError$(errorCode%):"zError"

;;; <summary>Get the version of Zlib being used.</summary>
;;; <returns>String containing the version of the Zlib library being used.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZlibVersion$():"zlibVersion"

; ----------------------------------------
; -- Utility Functions
; ----------------------------------------

;;; <summary>Estimates the size of buffer required to compress sourceLength bytes of data.</summary>
;;; <param name="sourceLength">The size of the buffer (in bytes) to compress.</param>
;;; <returns>The maximum size a buffer of sourceLength bytes would be after compression.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_CompressBound%(sourceLength%):"compressBound"

; ----------------------------------------
; -- Compression & Decompression
; ----------------------------------------

;;; <summary>Compress the bank sourceBank.</summary>
;;; <param name="destBank">Handle of a bank to place data in.</param>
;;; <param name="destSize">4 byte bank where the size of the compressed data will be stored.</param>
;;; <param name="sourceBank">A bank handled containing the data to be compressed.</param>
;;; <param name="sourceSize">The size of the source bank in bytes.</param>
;;; <returns>UNZ_OK if operation completed, or an error code if there was a problem.</returns>
;;; <remarks>Same as ZlibWapi_Compress2 called with compressionLevel of "ZIPAPI_DEFAULT_LEVEL".</remarks>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_Compress%(destBank*, destSize*, sourceBank*, sourceSize%): "compress"

;;; <summary>Compress the bank sourceBank with a level specified by compressionLevel.</summary>
;;; <param name="destBank">Handle of a bank to place data in.</param>
;;; <param name="destSize">4 byte bank where the size of the compressed data will be stored.</param>
;;; <param name="sourceBank">A bank handled containing the data to be compressed.</param>
;;; <param name="sourceSize">The size of the source bank in bytes.</param>
;;; <param name="compressionLevel">A value between 0 and 9, representing the level of compression to use. 1 is highest speed, 9 is highest compression.</param>
;;; <returns>UNZ_OK if operation completed, or an error code if there was a problem.</returns>
;;; <remarks>ZipApi_Compress wraps this function.</remarks>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_Compress2%(destBank*, destSize*, sourceBank*, sourceSize%, compressionLevel%): "compress2"

;;; <summary>Uncompresses sourceSize bytes from sourceBank, and places the result in destBank.</summary>
;;; <param name="destBank">The bank where the uncompressed data will be stored.</param>
;;; <param name="destSize">A bank containing the size of destBank in bytes. The bank should be 4 bytes long.</param>
;;; <param name="sourceBank">The bank containing the data to be uncompressed.</param>
;;; <param name="sourceSize">The size of the source bank in bytes.</param>
;;; <remarks>ZipApi_UnCompress wraps this function.</remarks>
;;; <returns>Z_OK if the operation was successful, otherwise it returns an error code.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnCompress%(destBank*, destSize*, sourceBank*, sourceSize%):"uncompress"

; ----------------------------------------
; -- Checksum Functions
; ----------------------------------------

;;; <summary>Computes a running Adler-32 checksum.</summary>
;;; <param name="adler">Previous adler value.</param>
;;; <param name="sourceBuffer">Bank of data to add to the current checksum.</param>
;;; <param name="sourceLength">Size of the source buffer.</param>
;;; <returns>An adler value for the buffer.</returns>
ZlibWapi_Adler32%(adler%, sourceBuffer*, sourceLength%):"adler32"

;;; <summary>Computes a running CRC-32 checksum.</summary>
;;; <param name="crcValue">Previous crc value.</param>
;;; <param name="sourceBuffer">Bank of data to add to the current checksum.</param>
;;; <param name="sourceLength">Size of the source buffer.</param>
;;; <returns>A CRC value.</returns>
ZlibWapi_Crc32%(crcValue%, sourceBuffer*, sourceLength%):"crc32"

; ----------------------------------------
; -- ZIP File Reading Functions
; ----------------------------------------

;;; <summary>Open a zip file for reading / writing.</summary>
;;; <param name="filePath">The path of the file to open.</param>
;;; <returns>Handle of the opened file, or 0 if there was an error.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzOpen%(filePath$):"unzOpen"

;;; <summary>Closes a zip file opened with ZlibWapi_UnzOpen.</summary>
;;; <param name="zipHandle">The zip file handle to close.</param name>
;;; <returns>UNZ_OK if there was no problem, or an error code if it failed.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzClose%(zipHandle):"unzClose"

;;; <summary>Gets information about a ZIP file that was opened with UnzOpen.</summary>
;;; <param name="zipHandle">The ZIP handle to get information about.</param>
;;; <param name="infoBank">A bank to hold the information in.</param>
;;; <remarks>Wrapped with ZipApi_GetGlobalInfo, which returns information in a type instead.</remarks>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzGetGlobalInfo%(zipHandle, infoBank*):"unzGetGlobalInfo"

;;; <summary>Gets the global comment for a ZIP file opened with UnzOpen.</summary>
;;; <param name="zipHandle">Handle of the ZIP file to get information about.</param>
;;; <param name="commentBank">A bank to hold the comment in.</param>
;;; <param name="commentLength">The size of the comment bank in bytes.</param>
;;; <returns></returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzGetGlobalComment%(zipHandle, commentBank*, commentLength%): "unzGetGlobalComment"

;;; <summary>Tries to locate the file "fileName" in the zipfile, and if successful sets the current file to the found file.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <param name="fileName">The name of the file to search for.</param>
;;; <param name="caseSensitive">If true, the search will be case sensitive.</param>
;;; <returns>ZIPAPI_UNZ_OK on found. ZIPAPI_UNZ_END_OF_LIST_OF_FILE on error.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzLocateFile%(zipHandle%, fileName$, caseSensitive%): "unzLocateFile"

;;; <summary>Sets the current file pointer to the first file in the ZIP.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <returns></returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzGoToFirstFile%(zipHandle%):"unzGoToFirstFile"

;;; <summary>Moves the current file pointer to the next file in the ZIP.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzGoToNextFile%(zipHandle%):"unzGoToNextFile"

;;; <summary>Gets information about the file pointed at by the current file pointer.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <param name="fileInfo">Bank that will contain file info.</param>
;;; <param name="fileName">Bank that will contain the file name.</param>
;;; <param name="fileNameBufferSize">The size of the fileName bank, in bytes.</param>
;;; <param name="extraField">A bank to hold the extra information field for this file.</param>
;;; <param name="extraFieldBufferSize">The size of the extraField bank, in bytes.</param>
;;; <param name="comment">A bank to hold the comment about this file.</param>
;;; <param name="commendBufferSize">The size of the comment bank, in bytes.</param>
;;; <remarks>Use ZipApi_GetCurrentFileInfo instead.</remarks>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzGetCurrentFileInfo%(zipHandle%, fileInfo*, fileName*, fileNameBufferSize%, extraField*, extraFieldBufferSize%, comment*, commentBufferSize%):"unzGetCurrentFileInfo"

;;; <summary>Opens the file pointed at by the current file pointer for reading.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzOpenCurrentFile%(zipHandle%):"unzOpenCurrentFile"

;;; <summary>Opens the password protected file pointed at by the current file pointer for reading.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen, or a ParamError code if password is incorrect.</param>
;;; <param name="password">The password to use when extracting the file.</param>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzOpenCurrentFilePassword%(zipHandle%, password$):"unzOpenCurrentFilePassword"

;;; <summary>Reads data from the currently open file and uncompresses it into memory.</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <param name="buffer">A bank that will hold the uncompressed data.</param>
;;; <param name="bufferLength">The size of the buffer bank, in bytes.</param>
;;; <returns>The number of uncompressed bytes that were read.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzReadCurrentFile%(zipHandle%, buffer*, bufferLength%):"unzReadCurrentFile"

;;; <summary>Closes the file that was opened with "ZlibWapi_UnzOpenCurrentFile".</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with UnzOpen.</param>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_UnzCloseCurrentFile%(zipHandle%):"unzCloseCurrentFile"

; ----------------------------------------
; -- Writing to ZIP files
; ----------------------------------------

;;; <summary>Opens a ZIP file for writing. If the file doesn't exist, it will be created.</summary>
;;; <param name="fileName">The name of the ZIP file to open.</param>
;;; <param name="appendMode">The file mode to use. Three choices: </param>
;;; <returns>The handle of the opened file, or an error code if something went wrong.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZipOpen%(fileName$, appendMode%):"zipOpen"

;;; <summary>Closes a file that has been opened with ZlibWapi_ZipOpen.</summary>
;;; <param name="zipHandle">The handle to close.</param>
;;; <param name="globalComment">A global comment for this file.</param>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZipClose%(zipHandle%, globalComment$):"zipClose"

;;; <summary>Create a new file entry in a ZIP file.</summary>
;;; <param name="zipHandle">Handle to a ZIP file opened with ZipOpen.</param>
;;; <param name="fileName">The file name of the entry to create.</param>
;;; <param name="fileInfo">Bank containing file date and property information.</param>
;;; <param name="extraFieldLocal">Bank containing extra information for the file.</param>
;;; <param name="extraFieldLocalSize">Size of the extraFieldLocal bank.</param>
;;; <param name="extraFieldGlobal">Bank containing extra information for the ZIP.</param>
;;; <param name="extraFieldGlobalSize">The size of the extraFieldGlobal bank.</param>
;;; <param name="comment">An optional comment for this file.</param>
;;; <param name="method">The compression method to use. Use 0 for store, or Z_DEFLATE for compression.</param>
;;; <param name="level">The level of compression to use (0 to 9).</param>
;;; <remarks>Use ZipApi_AddFile to save yourself a lot of trouble.</remarks>
;;; <returns>ZIPAPI_OK on success, or an error code if there was a problem.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZipOpenNewFileInZip%(zipHandle%, fileName$, fileInfo*, extraFieldLocal*, extraFieldLocalSize%, extraFieldGlobal*, extraFieldGlobalSize%, comment$, method%, level%):"zipOpenNewFileInZip"

;;; <summary>Create a new file entry in a ZIP file. This is an extended version of ZlibWapi_ZipOpenNewFileInZip.</summary>
;;; <param name="zipHandle">Handle to a ZIP file opened with ZipOpen.</param>
;;; <param name="fileName">The file name of the entry to create.</param>
;;; <param name="fileInfo">Bank containing file date and property information.</param>
;;; <param name="extraFieldLocal">Bank containing extra information for the file.</param>
;;; <param name="extraFieldLocalSize">Size of the extraFieldLocal bank.</param>
;;; <param name="extraFieldGlobal">Bank containing extra information for the ZIP.</param>
;;; <param name="extraFieldGlobalSize">The size of the extraFieldGlobal bank.</param>
;;; <param name="comment">An optional comment for this file.</param>
;;; <param name="method">The compression method to use. Use 0 for store, or Z_DEFLATE for compression.</param>
;;; <param name="level">The level of compression to use (0 to 9).</param>
;;; <param name="raw">Use 0</param>
;;; <param name="windowBits">Use const value -ZIPAPI_MAX_WBITS (minus).</param>
;;; <param name="memLevel">Use const value ZIPAPI_DEF_MEM_LEVEL.</param>
;;; <param name="strategy">Use const value ZIPAPI_DEFAULT_STRATEGY.</param>
;;; <param name="password">The password to encrypt the data with.</param>
;;; <param name="crcForCrypting">CRC value of the file data we are adding.</param>
;;; <remarks>Use ZipApi_AddFile to save yourself a lot of trouble.</remarks>
;;; <returns>ZIPAPI_OK on success, or an error code if there was a problem.</returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZipOpenNewFileInZip3%(zipHandle%, fileName$, fileInfo*, extraFieldLocal*, extraFieldLocalSize%, extraFieldGlobal*, extraFieldGlobalSize%, comment$, method%, level%, raw%, windowBits%, memLevel%, strategy%, password$, crcForCrypting%):"zipOpenNewFileInZip3"

;;; <summary>Writes the contents of a bank to an opened ZIP file entry. For use after opening an entry with "ZlibWapi_ZipOpenNewFileInZip".</summary>
;;; <param name="zipHandle">Handle to a ZIP file opened with ZipOpen.</param>
;;; <param name="fileData">Bank containing the data to write.</param>
;;; <param name="fileLength">The length of the "fileData" bank.</param>
;;; <returns></returns>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZipWriteFileInZip%(zipHandle%, fileData*, fileLength%):"zipWriteInFileInZip"

;;; <summary>Closes a file in a ZIP that was opened by "ZlibWapi_ZipOpenNewFileInZip".</summary>
;;; <param name="zipHandle">Handle of a ZIP file opened with ZipOpen.</param>
;;; <subsystem>Blitz.UserLibs.ZlibWapi</subsystem>
ZlibWapi_ZipCloseFileInZip%(zipHandle%):"zipCloseFileInZip"