package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum AttachFace implements StringRepresentable {
    FLOOR("floor"),
    WALL("wall"),
    CEILING("ceiling");

    private final String name;

    private AttachFace(final String pName) {
        this.name = pName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}