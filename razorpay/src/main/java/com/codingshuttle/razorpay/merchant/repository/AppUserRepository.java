package com.codingshuttle.razorpay.merchant.repository;

import com.codingshuttle.razorpay.merchant.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {


}
