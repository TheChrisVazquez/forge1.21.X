package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V2519 extends NamespacedSchema {
    public V2519(int pVersionKey, Schema pParent) {
        super(pVersionKey, pParent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
        pSchema.register(map, "minecraft:strider", () -> V100.equipment(pSchema));
        return map;
    }
}