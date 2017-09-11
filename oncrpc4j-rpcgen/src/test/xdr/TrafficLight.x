enum TrafficLightColor {
 RED = 1,
 ORANGE =2,
 GREEN = 3
};

struct light_state {
       TrafficLightColor color;
       hyper int timeRemaining;
};
program TRAFFICLIGHT {
    version  TRAFFICLIGHT_ONLY_ONE_VERS{
        TrafficLightColor getColor(void) = 1;
        TrafficLightColor setColor(TrafficLightColor color) = 2;
    } = 1;
} = 0x666;
