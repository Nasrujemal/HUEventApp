package com.hu.hueventapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.hu.hueventapp.model.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    LiveData<User> login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<User> getUserById(long userId);

    @Query("SELECT * FROM users WHERE isAdmin = 1")
    LiveData<List<User>> getAllAdmins();

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();
} 