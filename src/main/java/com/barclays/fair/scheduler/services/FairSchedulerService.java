package com.barclays.fair.scheduler.services;

import com.barclays.fair.scheduler.data.FairScheduler;
import com.barclays.fair.scheduler.data.FairSchedulerRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FairSchedulerService {

    private final FairSchedulerRepository repository;

    public FairSchedulerService(FairSchedulerRepository repository) {
        this.repository = repository;
    }

    public Optional<FairScheduler> get(Long id) {
        return repository.findById(id);
    }

    public FairScheduler update(FairScheduler entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<FairScheduler> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<FairScheduler> list(Pageable pageable, Specification<FairScheduler> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
