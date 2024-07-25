package com.barclays.fair.scheduler.data;

import jakarta.persistence.Entity;

@Entity
public class FairScheduler extends AbstractEntity {

    private String queue;
    private Integer weight;
    private Integer min_virtual_core;
    private Integer max_virtual_memory;
    private Integer max_running_apps;
    private String scheduling_policy;
    private boolean preemptable;

    public String getQueue() {
        return queue;
    }
    public void setQueue(String queue) {
        this.queue = queue;
    }
    public Integer getWeight() {
        return weight;
    }
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    public Integer getMin_virtual_core() {
        return min_virtual_core;
    }
    public void setMin_virtual_core(Integer min_virtual_core) {
        this.min_virtual_core = min_virtual_core;
    }
    public Integer getMax_virtual_memory() {
        return max_virtual_memory;
    }
    public void setMax_virtual_memory(Integer max_virtual_memory) {
        this.max_virtual_memory = max_virtual_memory;
    }
    public Integer getMax_running_apps() {
        return max_running_apps;
    }
    public void setMax_running_apps(Integer max_running_apps) {
        this.max_running_apps = max_running_apps;
    }
    public String getScheduling_policy() {
        return scheduling_policy;
    }
    public void setScheduling_policy(String scheduling_policy) {
        this.scheduling_policy = scheduling_policy;
    }
    public boolean isPreemptable() {
        return preemptable;
    }
    public void setPreemptable(boolean preemptable) {
        this.preemptable = preemptable;
    }

}
