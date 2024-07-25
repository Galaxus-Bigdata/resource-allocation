package com.barclays.fair.scheduler.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FairSchedulerRepository
        extends
            JpaRepository<FairScheduler, Long>,
            JpaSpecificationExecutor<FairScheduler> {

}
