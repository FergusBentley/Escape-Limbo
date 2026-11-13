package com.fergusb.escapelimbo.beacon;

import net.minecraft.util.StringRepresentable;

public enum RespawnBeaconState implements StringRepresentable {
    UNPRIMED("unprimed"),
    ACTIVE("active"),
    OBSTRUCTED("obstructed"),
    EMPTY("empty");

    public static final StringRepresentable.EnumCodec<RespawnBeaconState> CODEC = StringRepresentable.fromEnum(RespawnBeaconState::values);

    private final String name;

    RespawnBeaconState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}