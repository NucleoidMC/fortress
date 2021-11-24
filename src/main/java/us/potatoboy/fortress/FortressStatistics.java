package us.potatoboy.fortress;

import xyz.nucleoid.plasmid.game.stats.StatisticKey;

public class FortressStatistics {
    public static final StatisticKey<Integer> CAPTURES = StatisticKey.intKey(Fortress.identifier("captures"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> ROWS_CAPTURED = StatisticKey.intKey(Fortress.identifier("rows_captured"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> MODULES_PLACED = StatisticKey.intKey(Fortress.identifier("modules_placed"), StatisticKey.StorageType.TOTAL);
}
