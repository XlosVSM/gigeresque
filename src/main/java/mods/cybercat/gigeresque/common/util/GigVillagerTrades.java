package mods.cybercat.gigeresque.common.util;

import mods.cybercat.gigeresque.common.tags.GigTags;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.TreasureMapForEmeralds;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record GigVillagerTrades() {

    public static void addTrades() {
        List<VillagerTrades.ItemListing> gig_trades = new ArrayList<>(Arrays.asList(VillagerTrades.TRADES.get(VillagerProfession.CARTOGRAPHER).get(4)));
        gig_trades.add(new TreasureMapForEmeralds(13, GigTags.GIG_EXPLORER_MAPS, "filled_map.gig_dungeon", MapDecoration.Type.RED_MARKER, 12, 5));
        VillagerTrades.ItemListing[] resultlvl1 = new VillagerTrades.ItemListing[]{};
        VillagerTrades.TRADES.get(VillagerProfession.CARTOGRAPHER).put(4, gig_trades.toArray(resultlvl1));
        List<VillagerTrades.ItemListing> gig_trades2 = new ArrayList<>(Arrays.asList(VillagerTrades.TRADES.get(VillagerProfession.CARTOGRAPHER).get(5)));
        gig_trades2.add(new TreasureMapForEmeralds(13, GigTags.GIG_EXPLORER_MAPS, "filled_map.gig_dungeon", MapDecoration.Type.RED_MARKER, 12, 5));
        VillagerTrades.ItemListing[] resultlvl2 = new VillagerTrades.ItemListing[]{};
        VillagerTrades.TRADES.get(VillagerProfession.CARTOGRAPHER).put(5, gig_trades2.toArray(resultlvl2));
    }
}
