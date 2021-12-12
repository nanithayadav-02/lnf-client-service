package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

    @Query("select e from Client e where e.id = :id")
    Optional<Client> findByClientId(@Param("id") String id);

    @Query(value = "select extract(year from c.created_time) as year, " +
            " to_char(c.created_time, 'MON') as month, count(c.id) as count "
            + " from client as c group by year, month order by year desc", nativeQuery = true)
    List<StatisticsSummary> clientsByYearAndMonth();

}
