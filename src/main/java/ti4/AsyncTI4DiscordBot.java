package ti4;

import ti4.actors.ButtonInteractionActor;
import ti4.actors.CommandAutoCompleteActor;
import ti4.actors.CommandResponseActor;
import ti4.actors.SlashCommandDispatcher;
import ti4.actors.game.GameCommandActor;
import ti4.buttons.ButtonInteractionContext;
import ti4.buttons.Buttons;
import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;
//import ti4.message.BotLogger;
import ti4.generator.Mapper;
import ti4.generator.PositionMapper;
import ti4.generator.TileHelper;
import ti4.helpers.AliasHandler;
import ti4.helpers.GlobalSettings;
import ti4.helpers.GlobalSettings.ImplementedSettings;
import ti4.helpers.Storage;
import ti4.map.GameSaveLoadManager;
import ti4.message.BotLogger;
import ti4.message.MessageHelper;
import ti4.selections.SelectionManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.security.auth.login.LoginException;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class AsyncTI4DiscordBot {

    public static JDA jda;
    public static String userID;
    public static String guildPrimaryID;
    public static Guild guildPrimary;
    public static Guild guildSecondary;
    public static Guild guildTertiary;
    public static Guild guildQuaternary;
    public static Guild guildQuinary;
    public static Guild guildFogOfWar;
    public static Guild guildCommunityPlays;
    public static final Set<Guild> guilds = new HashSet<>();

    public static final long START_TIME_MILLISECONDS = System.currentTimeMillis();
    public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors())
    );
    public static final List<Role> adminRoles = new ArrayList<>();
    public static final List<Role> developerRoles = new ArrayList<>();
    public static final List<Role> bothelperRoles = new ArrayList<>();

    public static Map<String, Function<String, List<Command.Choice>>> autoCompleteFunctions = new HashMap<>();
    
    public static void registerAutoComplete(String commandName, Function<String, List<Command.Choice>> autoCompleteFunction) {
        if(autoCompleteFunctions.containsKey(commandName)) {
            throw new IllegalArgumentException("AutoComplete function for command " + commandName + " already exists.");
        }
        autoCompleteFunctions.put(commandName, autoCompleteFunction);
    }

    public static Map<String, Consumer<ButtonInteractionContext>> buttonHandlers = new HashMap<>();
    
    public static void registerButtonHandler(String buttonId, Consumer<ButtonInteractionContext> buttonHandlerFunction) {
        if(buttonHandlers.containsKey(buttonId)) {
            throw new IllegalArgumentException("Button handler for command " + buttonId + " already exists.");
        }
        buttonHandlers.put(buttonId, buttonHandlerFunction);
    }

    public static Map<String, Behavior<CommandMessage>> commandHandlers = Map.of(
        "game", GameCommandActor.create()
    );

    public static void main(String[] args) throws LoginException {
        GlobalSettings.loadSettings();
        GlobalSettings.setSetting(ImplementedSettings.READY_TO_RECEIVE_COMMANDS, false);
        
        ActorSystem<CommandMessage> commandDispatcherSystem = ActorSystem.create(SlashCommandDispatcher.create(AsyncTI4DiscordBot.commandHandlers), "commands");
        ActorSystem<CommandResponse> commmandResponseActorSystem = ActorSystem.create(CommandResponseActor.create(), "commandResponses");
        ActorSystem<ButtonInteractionEvent> buttonIneractionActorSystem = ActorSystem.create(ButtonInteractionActor.create(), "interactions");
        ActorSystem<CommandAutoCompleteInteractionEvent> commandAutoCompleteActor = ActorSystem.create(CommandAutoCompleteActor.create(), "autocompletes");

        // Initialize JDA and connect your bot
        jda = JDABuilder.createDefault(args[0])
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setEnableShutdownHook(false)
            .addEventListeners(new BotSystem(commandDispatcherSystem, commmandResponseActorSystem, buttonIneractionActorSystem, commandAutoCompleteActor))
            .build();

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            MessageHelper.sendMessageToBotLogWebhook("Error waiting for bot to get ready");
        }

        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.customStatus("STARTING UP: Connecting to Servers"));

        guildPrimaryID = args[2];
        userID = args[1];

        MessageHelper.sendMessageToBotLogWebhook("# `" + new Timestamp(System.currentTimeMillis()) + "`  BOT IS STARTING UP");

        // Primary HUB Server
        guildPrimary = jda.getGuildById(args[2]);
        BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildPrimary.getName());
        guilds.add(guildPrimary);

        // Community Plays TI
        if (args.length >= 4) {
            guildCommunityPlays = jda.getGuildById(args[3]);
            if (guildCommunityPlays != null) {
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildCommunityPlays.getName());
                guilds.add(guildCommunityPlays);
            }
        }

        // Async: FOW Chapter
        if (args.length >= 5) {
            guildFogOfWar = jda.getGuildById(args[4]);
            if (guildFogOfWar != null) {
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildFogOfWar.getName());
                guilds.add(guildFogOfWar);
            }
        }

        // Async: Stroter's Paradise
        if (args.length >= 6) {
            guildSecondary = jda.getGuildById(args[5]);
            if (guildSecondary != null) {
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildSecondary.getName());
                guilds.add(guildSecondary);
            }
        }

        // Async: Dreadn't
        if (args.length >= 7) {
            guildTertiary = jda.getGuildById(args[6]);
            if (guildTertiary != null) {
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildTertiary.getName());
                guilds.add(guildTertiary);
            }
        }

        // Async: War Sun Tzu
        if (args.length >= 8) {
            guildQuaternary = jda.getGuildById(args[7]);
            if (guildQuaternary != null) {
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildQuaternary.getName());
                guilds.add(guildQuaternary);
            }
        }

        // Async: Fighter Club
        if (args.length >= 9) {
            guildQuinary = jda.getGuildById(args[8]);
            if (guildQuinary != null) {
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  BOT STARTED UP: " + guildQuinary.getName());
                guilds.add(guildQuinary);
            }
        }

         // LOAD DATA
        BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  LOADING DATA");
        jda.getPresence().setActivity(Activity.customStatus("STARTING UP: Loading Data"));
        TileHelper.init();
        PositionMapper.init();
        Mapper.init();
        AliasHandler.init();
        Storage.init();
        SelectionManager.init();
        Buttons.init();
        initializeWhitelistedRoles();

        // LOAD GAMES
        BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  LOADING GAMES");
        jda.getPresence().setActivity(Activity.customStatus("STARTING UP: Loading Games"));
        GameSaveLoadManager.loadMaps();

        // RUN DATA MIGRATIONS
        BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  CHECKING FOR DATA MIGRATIONS");
        DataMigrationManager.runMigrations();
        BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "`  FINISHED CHECKING FOR DATA MIGRATIONS");

        // BOT IS READY
        GlobalSettings.setSetting(ImplementedSettings.READY_TO_RECEIVE_COMMANDS, true);
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Async TI4"));
        BotLogger.log("# `" + new Timestamp(System.currentTimeMillis()) + "`  FINISHED LOADING GAMES");

        // Register Shutdown Hook to run when SIGTERM is recieved from docker stop
        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.customStatus("BOT IS SHUTTING DOWN"));
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "` SHUTDOWN PROCESS STARTED");
                GlobalSettings.setSetting(ImplementedSettings.READY_TO_RECEIVE_COMMANDS, false);
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "` NO LONGER ACCEPTING COMMANDS");
                TimeUnit.SECONDS.sleep(10); // wait for current commands to complete
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "` SAVING GAMES");
                GameSaveLoadManager.saveMaps();
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "` GAMES HAVE BEEN SAVED");
                BotLogger.log("`" + new Timestamp(System.currentTimeMillis()) + "` SHUTDOWN PROCESS COMPLETE");
                TimeUnit.SECONDS.sleep(1); // wait for BotLogger
                jda.shutdown();
                jda.awaitShutdown();
                mainThread.join();
            } catch (Exception e) {
                MessageHelper.sendMessageToBotLogWebhook("Error encountered within shutdown hook:\n> " + e.getMessage());
                e.printStackTrace();
            }
        }));
    }

    private static void initializeWhitelistedRoles() {
        //ADMIN ROLES
        adminRoles.add(jda.getRoleById("943596173896323072")); // Async Primary (Hub)
        adminRoles.add(jda.getRoleById("1090914497352446042")); // Async Secondary (Stroter's Paradise)
        adminRoles.add(jda.getRoleById("1146511484264906814")); // Async Tertiary (Dreadn't)
        adminRoles.add(jda.getRoleById("1176104225978204167")); // Async Quaternary (War Sun Tzu)
        adminRoles.add(jda.getRoleById("1209956332380229678")); // Async Quinary (Fighter Club)
        adminRoles.add(jda.getRoleById("1062804021385105500")); // FoW Server
        adminRoles.add(jda.getRoleById("1067866210865250445")); // PrisonerOne's Test Server
        adminRoles.add(jda.getRoleById("1060656344581017621")); // Softnum's Server
        adminRoles.add(jda.getRoleById("1109657180170371182")); // Jazz's Server
        adminRoles.add(jda.getRoleById("1100120742093406319")); // Moo's Server
        adminRoles.add(jda.getRoleById("1126610851034583050")); // Fin's Server
        adminRoles.add(jda.getRoleById("824111008863092757")); // Fireseal's Server
        adminRoles.add(jda.getRoleById("336194595501244417")); // tedw4rd's Server
        adminRoles.add(jda.getRoleById("1149705227625316352")); // who dis
        adminRoles.add(jda.getRoleById("1178659621225889875")); // Jepp2078's Server
        adminRoles.add(jda.getRoleById("1215451631622164610")); // Sigma's Server
        adminRoles.add(jda.getRoleById("1226068025464197160")); // Rintsi's Server
        adminRoles.removeIf(Objects::isNull);

        //DEVELOPER ROLES
        developerRoles.addAll(adminRoles); //admins can also execute developer commands
        developerRoles.add(jda.getRoleById("947648366056185897")); // Async Primary (Hub)
        developerRoles.add(jda.getRoleById("1090958278479052820")); // Async Secondary (Stroter's Paradise)
        developerRoles.add(jda.getRoleById("1146529125184581733")); // Async Tertiary (Dreadn't)
        developerRoles.add(jda.getRoleById("1176104225978204166")); // Async Quaternary (War Sun Tzu)
        developerRoles.add(jda.getRoleById("1209956332380229677")); // Async Quinary (Fighter Club)
        developerRoles.add(jda.getRoleById("1088532767773564928")); // FoW Server
        developerRoles.add(jda.getRoleById("1215453013154734130")); // Sigma's Server
        developerRoles.add(jda.getRoleById("1226068105071956058")); // Rintsi's Server
        developerRoles.removeIf(Objects::isNull);

        //BOTHELPER ROLES
        bothelperRoles.addAll(adminRoles); //admins can also execute bothelper commands
        bothelperRoles.add(jda.getRoleById("1166011604488425482")); // Async Primary (Hub)
        bothelperRoles.add(jda.getRoleById("1090914992301281341")); // Async Secondary (Stroter's Paradise)
        bothelperRoles.add(jda.getRoleById("1146539257725464666")); // Async Tertiary (Dreadn't)
        bothelperRoles.add(jda.getRoleById("1176104225978204164")); // Async Quaternary (War Sun Tzu)
        bothelperRoles.add(jda.getRoleById("1209956332380229675")); // Async Quinary (Fighter Club)
        bothelperRoles.add(jda.getRoleById("1088532690803884052")); // FoW Server
        bothelperRoles.add(jda.getRoleById("1063464689218105354")); // FoW Server Game Admin
        bothelperRoles.add(jda.getRoleById("1131925041219653714")); // Jonjo's Server
        bothelperRoles.add(jda.getRoleById("1215450829096624129")); // Sigma's Server
        bothelperRoles.add(jda.getRoleById("1226068245010710558")); // Rintsi's Server
        bothelperRoles.removeIf(Objects::isNull);
    }

    public static boolean isReadyToReceiveCommands() {
        return GlobalSettings.getSetting(GlobalSettings.ImplementedSettings.READY_TO_RECEIVE_COMMANDS.toString(), Boolean.class, false);
    }

    public static <T> CompletableFuture<T> completeAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, THREAD_POOL)
            .handle((result, exception) -> {
                if (exception != null) {
                    BotLogger.log("Unable to complete async process.", exception);
                    return null;
                }
                return result;
            });
    }

    public static List<Category> getAvailablePBDCategories() {
        return guilds.stream()
            .flatMap(guild -> guild.getCategories().stream())
            .filter(category -> category.getName().toUpperCase().startsWith("PBD #"))
            .toList();
    }
}
