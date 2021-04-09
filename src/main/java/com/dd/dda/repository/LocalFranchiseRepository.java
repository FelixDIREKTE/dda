package com.dd.dda.repository;

import com.dd.dda.model.sqldata.LocalFranchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocalFranchiseRepository  extends JpaRepository<LocalFranchise, Long> {

    @Query(value = "select * from local_franchise WHERE zipcode = :plz", nativeQuery = true)
    List<LocalFranchise> findAllByZip(String plz);
}
