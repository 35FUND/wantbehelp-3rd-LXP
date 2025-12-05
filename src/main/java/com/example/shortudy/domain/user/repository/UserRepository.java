package com.example.shortudy.domain.user.repository;

import com.example.shortudy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//쉐도우 복싱용 유저
@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
}

