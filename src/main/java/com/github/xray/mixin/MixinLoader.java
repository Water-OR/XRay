package com.github.xray.mixin;

import com.github.xray.Tags;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;

public class MixinLoader implements IEarlyMixinLoader {
        @Override
        public List<String> getMixinConfigs() {
                return Collections.singletonList("mixins." + Tags.MOD_ID + ".json");
        }
}
