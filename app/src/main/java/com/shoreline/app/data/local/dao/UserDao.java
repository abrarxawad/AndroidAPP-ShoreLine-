package com.shoreline.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shoreline.app.data.local.entity.UserEntity;

import java.util.List;

/**
 * SYLLABUS: Room Data Access Object (DAO) - User profile queries
 */
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Update
    void updateUser(UserEntity user);

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users ORDER BY created_at DESC LIMIT 1")
    UserEntity getCurrentActiveUser();

    @Query("DELETE FROM users")
    void clearAllUsers();
}
