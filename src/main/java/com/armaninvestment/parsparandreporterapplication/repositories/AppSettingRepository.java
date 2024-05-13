package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
}