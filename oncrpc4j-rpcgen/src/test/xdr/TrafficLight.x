enum TrafficLightColor {
 RED = 1,
 ORANGE =2,
 GREEN = 3
};

enum ReducedTrafficLightColor{
 ReducedRED = RED	
};

enum RainbowTrafficLightColor{
 BLUE = 1,
 MAGENTA = BLUE,
 RAINBOW_RED = ReducedRED
};

/* This definition triggers a numberFormatException both in
   base and in modified code
enum DarkTrafficLightColor {
 DARK_RED,
 DARK_GREEN,
 DARK_ORANGE
};
*/
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
