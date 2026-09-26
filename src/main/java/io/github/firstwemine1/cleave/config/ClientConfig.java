package io.github.firstwemine1.cleave.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import io.github.firstwemine1.cleave.utils.PredicateUtils;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public final class ClientConfig {
    public final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue MOD_DISABLED;
    public final ModConfigSpec.BooleanValue MOD_RESTRICTED;

    public final ModConfigSpec.BooleanValue GIANT_VEIN_MINING_DISABLED;
    public final ModConfigSpec.BooleanValue GIANT_VEIN_MINING_RESTRICTED;

    public final ModConfigSpec.BooleanValue TREE_FELLING_DISABLED;
    public final ModConfigSpec.BooleanValue TREE_FELLING_RESTRICTED;

    public final ModConfigSpec.BooleanValue TUNNELING_RESTRICTED;

    public final ModConfigSpec.BooleanValue FORCE_RESTRICTED;

    public final ModConfigSpec.EnumValue<TriggerActions> TRIGGER_WHEN;

    public ClientConfig(UUID playerUUID) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("cleave-player-" + playerUUID.toString());

        builder.push("self-config");
        TRIGGER_WHEN = builder.comment("Condition for when the mod is triggered")
                .defineEnum(
                        "trigger_when",
                        CleaveConfig.TRIGGER_WHEN_DEFAULT,
                        EnumGetMethod.NAME_IGNORECASE,
                        o -> o instanceof TriggerActions || (o instanceof String str && Arrays.stream(TriggerActions.values()).anyMatch(t -> t.name().equalsIgnoreCase(str))),
                        TriggerActions.class);
        builder.pop();

        builder.push("toggles");
        MOD_DISABLED = builder.comment("Whether or not the player disabled the mod for themselves")
                .define("mod_disabled", CleaveConfig.MOD_DEFAULT_DISABLED);

        GIANT_VEIN_MINING_DISABLED = builder.comment("Whether or not the player disabled giant vein mining for themselves")
                .define("giant_vein_mining_disabled", CleaveConfig.GIANT_VEIN_MINING_DEFAULT_DISABLED);

        TREE_FELLING_DISABLED = builder.comment("Whether or not the player disabled tree felling for themselves")
                .define("tree_felling_disabled", CleaveConfig.TREE_FELLING_DEFAULT_DISABLED);
        builder.pop();

        builder.push("restrictions");
        MOD_RESTRICTED = builder.comment("Whether or not the mod was restricted by an operator")
                .define("mod_restricted", CleaveConfig.MOD_DEFAULT_RESTRICTED);

        GIANT_VEIN_MINING_RESTRICTED = builder.comment("Whether or not giant vein mining was restricted by an operator")
                .define("giant_vein_mining_restricted", CleaveConfig.GIANT_VEIN_MINING_DEFAULT_RESTRICTED);

        TREE_FELLING_RESTRICTED = builder.comment("Whether or not tree felling was restricted by an operator")
                .define("tree_felling_restricted", CleaveConfig.TREE_FELLING_DEFAULT_RESTRICTED);

        TUNNELING_RESTRICTED = builder.comment("Whether or not tunneling was restricted by an operator")
                .define("tunneling_restricted", CleaveConfig.TUNNELING_DEFAULT_RESTRICTED);

        FORCE_RESTRICTED = builder.comment("Whether or not /cleave force was restricted by an operator")
                .define("force_restricted", CleaveConfig.FORCE_DEFAULT_RESTRICTED);
        builder.pop();

        builder.pop();
        SPEC = builder.build();
    }
}
