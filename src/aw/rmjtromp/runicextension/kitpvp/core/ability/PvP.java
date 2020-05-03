package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class PvP extends Ability {
	
	public PvP() {
		super(3);
	}
	
	@Override
	public boolean onAbility(KitPlayer player) {
		player.playSound(player.getEyeLocation(), Sound.FIZZ, 1, 1);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3*20, 0, true, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3*20, 0, true, false));
		return true;
	}

}
