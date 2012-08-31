package syam.LightLevelBlocker.Listener;

import java.util.logging.Logger;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import syam.LightLevelBlocker.LightLevelBlocker;
import syam.LightLevelBlocker.Util.Actions;

public class LLBBlockListener implements Listener{
	// logger
	public final static Logger log = LightLevelBlocker.log;
	public final static String logPrefix = LightLevelBlocker.logPrefix;
	public final static String msgPrefix = LightLevelBlocker.msgPrefix;

	public static LightLevelBlocker plugin;
	public LLBBlockListener(final LightLevelBlocker instance){
		this.plugin = instance;
	}

	/* 登録するイベントはここから下に */

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();

		// 権限持ち、またはクリエイティブなら何もしない
		if (player.hasPermission("llb.bypass") || player.getGameMode().equals(GameMode.CREATIVE)){
			return;
		}

		boolean denied = false;

		// 明るさレベルを取得
		byte level = 0;
		for (BlockFace y : BlockFace.values()){
			byte l = block.getRelative(y).getLightLevel();
			if (l > level) level = l;
		}

		if (level <= plugin.getConfigs().blockLevel){
			// ワールドチェック
			if (plugin.getConfigs().bypassWorlds.isEmpty() || plugin.getConfigs().bypassWorlds == null){
				denied = true;
			}else{
				if (!plugin.getConfigs().bypassWorlds.contains(player.getWorld().getName())){
					denied = true;
				}
			}
		}

		// denied
		if (denied){
			event.setCancelled(true);
			Actions.message(null, player, plugin.getConfigs().msg);
		}
	}
}
