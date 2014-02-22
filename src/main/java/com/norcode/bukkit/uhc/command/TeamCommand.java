package com.norcode.bukkit.uhc.command;

import com.norcode.bukkit.uhc.UHC;
import com.norcode.bukkit.uhc.UHCError;
import com.norcode.bukkit.uhc.chat.ClickAction;
import com.norcode.bukkit.uhc.chat.Text;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TeamCommand extends BaseCommand {
	public TeamCommand(JavaPlugin plugin) {
		super(plugin, "team", null, "uhc.commands.team", new String[] {
			"/team <teamName> - create a new team.",
			"/team invite <playerName> - invite a player to join the team",
			"/team join <teamName> - join a team you've been invited to",
            "/team kick <playerName> - kicks a player from the team",
            "/team leave - leaves the team"
		});
		this.registerSubcommand(new InviteCommand(plugin));
		this.registerSubcommand(new JoinCommand(plugin));
		this.registerSubcommand(new LeaveCommand(plugin));
		plugin.getCommand("team").setExecutor(this);
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (args.size() == 0) {
			showHelp(commandSender, label, args);
		} else {
			String teamName = StringUtils.join(args, " ");
			Team onTeam = ((UHC) plugin).getMainScoreboard().getPlayerTeam((Player) commandSender);
			if (onTeam != null) {
				throw new CommandError("You are already on a team.");
			}
			try {
				Team team = ((UHC) plugin).registerTeam(teamName, (Player) commandSender);
				commandSender.sendMessage(new String[] {
						ChatColor.BOLD + "Successfully Created " + team.getDisplayName() + "[" + team.getName() + "]",
						"You may now invite players using /team invite <playerName>",
				});
			} catch (UHCError error) {
				throw new CommandError(error.getMessage());
			}
		}
	}

	public static class LeaveCommand extends BaseCommand {
        public LeaveCommand(JavaPlugin plugin) {
            super(plugin, "leave", new String[] {"quit"}, "uhc.commands.team.leave", new String[] {
                "Leave your current team",
                "If you are the team captain the team will be destroyed."
            });
        }

        @Override
        protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
            Team team = ((UHC) plugin).getMainScoreboard().getPlayerTeam((Player) commandSender);
			commandSender.sendMessage("You have left the team.");
			Objective teamMemberObjective = ((UHC) plugin).getTeamScoreboard().getObjective("members");
			Score score = teamMemberObjective.getScore(Bukkit.getOfflinePlayer(team.getName()));
			OfflinePlayer capn = ((UHC) plugin).getTeamCaptain(team.getName());

			if (team == null) {
                throw new CommandError("You are not on a team.");
            }
            if (commandSender.getName().equals(capn.getName())) {
                //disband the entire team if the captain leaves.
                for (OfflinePlayer p: team.getPlayers()) {
                    team.removePlayer(p);
                    if (p.isOnline()) {
                        Player onlinePlayer = (Player) p;
                        onlinePlayer.sendMessage("Your team has been disbanded.");
                    }
                }
				((UHC) plugin).getTeamScoreboard().resetScores(Bukkit.getOfflinePlayer(team.getName()));
                team.unregister();
                return;
            }
            team.removePlayer(((Player) commandSender).getPlayer());

            score.setScore(score.getScore() - 1);
            if (capn.isOnline()) {
                Player onlineCaptain = (Player) capn;
                onlineCaptain.sendMessage(((Player) commandSender).getPlayerListName() + " has left your team.");
            }
        }
    }

    public static class InviteCommand extends BaseCommand {
		public InviteCommand(JavaPlugin plugin) {
			super(plugin, "invite", new String[] {"add"}, "uhc.commands.team.invite", new String[] {
				"Invite another player to join the team.  ",
				"Only the player who created the team may invite other players to join"
			});
		}

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			List<String> results = new ArrayList<String>();
			for (Player p: plugin.getServer().getOnlinePlayers()) {
				if (!p.hasPermission("uhc.staff")) {
					Team team = ((UHC) plugin).getMainScoreboard().getPlayerTeam(p);
					if (team == null) {
						results.add(p.getName());
					}
				}
			}
			return results;
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			Team team = ((UHC) plugin).getMainScoreboard().getPlayerTeam((Player) commandSender);
			if (team == null) {
				throw new CommandError("You are not on a team.");
			}
			OfflinePlayer capn = ((UHC) plugin).getTeamCaptain(team.getName());
			if (!commandSender.getName().equals(capn.getName())) {
				throw new CommandError("Only the team captain can invite other players to join.");
			}
			if (team.getSize() == ((UHC)plugin).getTeamSize()) {
				throw new CommandError("Team is already at capacity.");
			}
			String playerName = args.peek();
			List<Player> players = plugin.getServer().matchPlayer(playerName);
			if (players.size() == 0) {
				throw new CommandError("Unknown Player: " + playerName);
			} else if (players.size() > 1) {
				throw new CommandError("More than one player matches: `" + playerName + "`");
			}
			Player player = players.get(0);
			Team onTeam = ((UHC) plugin).getMainScoreboard().getPlayerTeam(player);
			if (onTeam != null) {
				throw new CommandError(player.getName() + " is already on a team.");
			}
			if (!player.hasMetadata("teamInvites")) {
				player.setMetadata("teamInvites", new FixedMetadataValue(plugin, new ArrayList<String>()));
			}
			List<String> invites = (List<String>) player.getMetadata("teamInvites").get(0).value();
			if (invites.contains(team.getName())) {
				throw new CommandError(player.getName() + "Has already been invited to the team.");
			}
			invites.add(team.getName());
			Text link = new Text("Click Here")
					.setClick(ClickAction.RUN_COMMAND,
							"/team join " + team.getName())
					.setHoverText("Click here to join " + team.getDisplayName())
					.append(new Text(" to join " + team.getDisplayName()));
			new Text(commandSender.getName() +
					" has invited you to join '"
					+ team.getDisplayName() + ".'")
					.append(link).sendTo(player);

			commandSender.sendMessage(ChatColor.BOLD + "Invitation has been sent.");
		}
	}

	public static class JoinCommand extends BaseCommand {
		public JoinCommand(JavaPlugin plugin) {
			super(plugin, "join", null, "uhc.comands.team.join", new String[] {
					"Join a team you have previously been invited to.  ",
			});
		}

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			List<String> results = new ArrayList<String>();
			if (sender instanceof Player) {
				if (((Player) sender).hasMetadata("teamInvites")) {
					List<String> invites = (List<String>) ((Player) sender).getMetadata("teamInvites").get(0).value();
					results.addAll(invites);
				}
			}
			return results;
		}

		@Override
		protected void onExecute(CommandSender sender, String label, LinkedList<String> args) throws CommandError {
			if (!(sender instanceof Player)) {
				throw new CommandError("Only players can join teams.");
			}
			Player player = ((Player) sender);
			Team onTeam = ((UHC) plugin).getMainScoreboard().getPlayerTeam(player);
			if (onTeam != null) {
				throw new CommandError("You are already on a team!");
			}
			Team team = ((UHC) plugin).getMainScoreboard().getTeam(args.peek());
			if (team == null) {
				throw new CommandError("Unknown Team: " + args.peek());
			}
			if (!player.hasMetadata("teamInvites")) {
				throw new CommandError("You have not been invited to join " + team.getDisplayName());
			}
			List<String> invites = (List<String>) player.getMetadata("teamInvites").get(0).value();
			if (!invites.contains(team.getName())) {
				throw new CommandError("You have not been invited to join " + team.getDisplayName());
			}
			if (team.getSize() == ((UHC)plugin).getTeamSize()) {
				throw new CommandError("That team already has the maximum number of players.");
			}
			team.addPlayer(player);
			sender.sendMessage("You have joined " + team.getDisplayName());
			for (OfflinePlayer op: team.getPlayers()) {
				if (op.isOnline()) {
					op.getPlayer().sendMessage(player.getName() + " has joined the team.");
				}
			}
			Objective teamMemberObjective = ((UHC) plugin).getTeamScoreboard().getObjective("members");
			Score score = teamMemberObjective.getScore(Bukkit.getOfflinePlayer(team.getName()));
			score.setScore(score.getScore() + 1);
			((UHC) plugin).checkReady();
		}
	}
}
