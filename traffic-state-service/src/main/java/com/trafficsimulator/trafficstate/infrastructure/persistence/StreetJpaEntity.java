package com.trafficsimulator.trafficstate.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "streets")
public class StreetJpaEntity {

    @Id
    private String id;
    private String name;
    @Column(name = "from_intersection_id")
    private String fromIntersectionId;
    @Column(name = "to_intersection_id")
    private String toIntersectionId;
    @Column(name = "hourly_capacity")
    private int hourlyCapacity;
    @Column(name = "current_volume")
    private int currentVolume;
    @Column(name = "traffic_light_count")
    private int trafficLightCount;
    @Column(name = "green_ratio")
    private double greenRatio;

    protected StreetJpaEntity() { }

    public StreetJpaEntity(String id, String name, String fromIntersectionId, String toIntersectionId,
                           int hourlyCapacity, int currentVolume, int trafficLightCount, double greenRatio) {
        this.id = id;
        this.name = name;
        this.fromIntersectionId = fromIntersectionId;
        this.toIntersectionId = toIntersectionId;
        this.hourlyCapacity = hourlyCapacity;
        this.currentVolume = currentVolume;
        this.trafficLightCount = trafficLightCount;
        this.greenRatio = greenRatio;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFromIntersectionId() { return fromIntersectionId; }
    public String getToIntersectionId() { return toIntersectionId; }
    public int getHourlyCapacity() { return hourlyCapacity; }
    public int getCurrentVolume() { return currentVolume; }
    public int getTrafficLightCount() { return trafficLightCount; }
    public double getGreenRatio() { return greenRatio; }
}
