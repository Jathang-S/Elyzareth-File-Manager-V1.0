package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FileItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY sortOrder ASC, id ASC")
    fun getAllFiles(): Flow<List<FileItemEntity>>

    @Query("SELECT * FROM files ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllFilesSync(): List<FileItemEntity>

    @Query("SELECT * FROM files WHERE folderId = :folderId ORDER BY sortOrder ASC, id ASC")
    fun getFilesByFolder(folderId: Long): Flow<List<FileItemEntity>>

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getFileById(id: Long): FileItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<FileItemEntity>)

    @Update
    suspend fun updateFile(file: FileItemEntity)

    @Update
    suspend fun updateFiles(files: List<FileItemEntity>)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("UPDATE files SET folderId = :newFolderId WHERE id = :fileId")
    suspend fun moveFileToFolder(fileId: Long, newFolderId: Long)

    @Query("SELECT COUNT(*) FROM files")
    suspend fun getFileCount(): Int
}
