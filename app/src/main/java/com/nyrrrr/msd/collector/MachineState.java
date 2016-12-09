package com.nyrrrr.msd.collector;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by nyrrrr on 09.12.2016.
 */

enum MachineState {



        INIT {
            @Override
            public Set<MachineState> possibleFollowUps() {
                return EnumSet.of(CAPTURE, KEYLOGGER);
            }
        },

        CAPTURE {
            @Override
            public Set<MachineState> possibleFollowUps() {
                return EnumSet.of(SAVE);
            }
        },

        KEYLOGGER {
            @Override
            public Set<MachineState> possibleFollowUps() {
                return EnumSet.of(SAVE);
            }
        },

        SAVE {
            @Override
            public Set<MachineState> possibleFollowUps() {
                return EnumSet.of(INIT);
            }
        };

        public Set<MachineState> possibleFollowUps() {
            return EnumSet.noneOf(MachineState.class);
        }


}
