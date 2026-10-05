package co.bracesoftware.erosion;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import co.bracesoftware.erosion.ErosionCore.ErosionDynamicItem;
import co.bracesoftware.erosion.ErosionExceptions.ErosionCommandExceptions.ErosionCommandParserException;
import co.bracesoftware.erosion.ErosionExceptions.ErosionCommandExceptions.ErosionCommandSetupException;
import co.bracesoftware.erosion.world.ErosionRegistry;
import co.bracesoftware.libs.minecraft_text_formatter.Text;

@EventBusSubscriber(modid = Erosion.MODID)
public class ErosionCommandProcessor
{
    @SubscribeEvent
    public static void entry(RegisterCommandsEvent e)
    {
        e.getDispatcher().register(
            Commands.literal(Erosion.MODID)
            .then(
                Commands.argument("arguments", StringArgumentType.greedyString())
                .executes(
                    c -> {
                        String msg = StringArgumentType.getString(c, "arguments");
                        List<String> list = new ArrayList<>(List.of(msg.split(" ")));
                        process(c.getSource(), list);
                        return 1;
                    }
                )
            )
            .executes(
                c -> {
                    process(c.getSource(), new ArrayList<>());
                    return 1;
                }
            )
        );
        return;
    }

    // ==================INTERNAL IMPL==================== //
    public static class ErosionCommand extends ErosionDynamicItem
    {
        private final BiConsumer<CommandSourceStack, List<String>> what;
        private final String helpInfo;
        private final boolean adminCommand;

        public ErosionCommand(
            String n, BiConsumer<CommandSourceStack, List<String>> w,
            String h, boolean ww
        ) throws ErosionCommandSetupException
        {
            this.name = n;
            this.what = w;
            this.adminCommand = ww;
            this.helpInfo = h;

            this.setupAntiDuplicationSystem();

            if(h.isEmpty() || h.isBlank())
            {
                throw new ErosionCommandSetupException("Help info cannot be blank.");
            }
            if(n.isEmpty() || n.isBlank())
            {
                throw new ErosionCommandSetupException("Command name cannot be blank.");
            }
        }

        public void call(CommandSourceStack s, List<String> args)
        {
            this.what.accept(s, args);
        }

        public String getHelpDescription()
        {
            return this.helpInfo;
        }

        @Override 
        public void setup()
        {
            ErosionUtils.Log("Setting up command -> " + this.name);
            this.preventDuplication(antiDuplicator);
            return;
        }

        @Override 
        public void discard()
        {
            ErosionUtils.Log("Discarding command -> " + this.name);
            this.discardDuplicationPreventionSys(antiDuplicator);
            return;
        }
    }

    public static final ErosionCommand MOD_STATUS = new ErosionCommand(
        ErosionRegistry.RawRegistry.CommandNames.MOD_STATUS.getId(),
        ErosionCommandProcessor::handleStatus,
        ErosionConfig.ForCommands.EMPTY_ARGUMENTS, false
    );
    public static final ErosionCommand RELOAD_CONFIG = new ErosionCommand(
        ErosionRegistry.RawRegistry.CommandNames.RELOAD_CONFIG.getId(),
        ErosionCommandProcessor::reloadCfg,
        ErosionConfig.ForCommands.EMPTY_ARGUMENTS, true
    );

    public static final ErosionCommand VIEW_CONFIG = new ErosionCommand(
        ErosionRegistry.RawRegistry.CommandNames.VIEW_CONFIG.getId(),
        ErosionCommandProcessor::viewCfg,
        ErosionConfig.ForCommands.EMPTY_ARGUMENTS, true
    );
    public static final ErosionCommand SET_CONFIG = new ErosionCommand(
        ErosionRegistry.RawRegistry.CommandNames.SET_CONFIG.getId(),
        ErosionCommandProcessor::setCfg,
        "< config_identifier value >", true
    );

    public static final List<ErosionCommand> COMMAND_LIST = List.of(
        MOD_STATUS, RELOAD_CONFIG,
        VIEW_CONFIG, SET_CONFIG
    );

    public static void setupCommands()
    {
        for(var m : COMMAND_LIST)
        {
            m.setup();
        }
    }

    public static void discardCommands()
    {
        for(var m : COMMAND_LIST)
        {
            m.discard();
        }
    }

    ///////////////////////////

    public static final boolean hasPermsForCommand(CommandSourceStack s)
    {
        boolean r = s.hasPermission(2);
        if(!r)
        {
            ErosionUtils.Misc.sendMsg(s, "You do not have required permissions to run this command.");
        }
        return r;
    }

    public static void process(
        CommandSourceStack s, List<String> args
    ) throws ErosionCommandParserException
    {
        if(args.isEmpty())
        {
            ErosionUtils.Misc.sendMsg(s,"List of commands:");

            for(var cmd : COMMAND_LIST)
            {
                var c = Component.literal("   ").append(Component.literal(cmd.name).withStyle(ChatFormatting.YELLOW)).append(" ")
                .append(Component.literal(cmd.getHelpDescription()).withStyle(ChatFormatting.GRAY));
                s.sendSystemMessage(c);
            }
            return;
        }

        args.removeIf(str -> (
            str == null ||
            str.isBlank() ||
            str.isEmpty()
        ));

        for(var cmd : COMMAND_LIST)
        {
            if(args.get(0).equals(cmd.name))
            {
                if(cmd.adminCommand)
                {
                    if(!hasPermsForCommand(s))
                    {
                        return;
                    }
                }
                args.remove(0);
                cmd.call(s, args);
                return;
            }
        }

        ErosionUtils.Misc.sendMsg(s, "No such command!");
        return;
    }

    // ================== ACTUAL COMMANDS ==================== //

    public static final void handleStatus(CommandSourceStack s, List<String> args)
    {
        if(!args.isEmpty())
        {
            ErosionUtils.Misc.sendMsg(s, "This command takes in no arguments!");
            return;
        }
        ErosionUtils.Misc.sendMsg(s, ErosionUtils.getStatus());
        return;
    }
    
    public static final void reloadCfg(CommandSourceStack s, List<String> args)
    {
        if(!args.isEmpty())
        {
            ErosionUtils.Misc.sendMsg(s, "This command takes in no arguments!");
            return;
        }
        
        ErosionUtils.Misc.sendMsg(s, "Reloading mod configuration...");
        ErosionConfig.ServerConfig.LoadModConfig();
        ErosionUtils.Misc.sendMsg(s, "Configuration reloaded.");
        
        return;
    }

    public static final void viewCfg(CommandSourceStack s, List<String> args)
    {
        ErosionUtils.Misc.sendMsg(s, "Configuration:");

        printConfig(s);
        return;
    }

    public static final void printConfig(CommandSourceStack s)
    {
        for(var c : ErosionConfig.ServerConfig.viewConfiguration())
        {
            ErosionUtils.Misc.sendMsg(s,c);
        }
    }

    public static final void setCfg(CommandSourceStack s, List<String> args)
    {
        if(args.size() != 2)
        {
            ErosionUtils.Misc.sendMsg(s, "Insufficient argument list!");
            return;
        }

        String config = args.get(0);
        String value = args.get(1);
        String newValue = null;

        if(ErosionPerformanceConfig.contains(config))
        {
            try
            {
                ErosionPerformanceConfig.set(config, value);
                ErosionPerformanceConfig.save(
                    java.nio.file.Path.of(ErosionConfig.ServerConfig.CONFIG_FOLDER, "performance.properties"),
                    ErosionUtils::Log);
                ErosionUtils.Misc.sendMsg(s, "Changed " + config + " to " + ErosionPerformanceConfig.get(config)
                    + ". Check the log for any file-save errors.");
            }
            catch(IllegalArgumentException e)
            {
                ErosionUtils.Misc.sendMsg(s, "Invalid value: " + e.getMessage());
            }
            return;
        }

        for(var c : ErosionConfig.ServerConfig.MOD_CONFIG)
        {
            if(c.id.equals(config))
            {
                if(c.getConfigClass().equals(Boolean.class))
                {
                    if(
                        !(value.equals("true")) &&
                        !(value.equals("false"))
                    )
                    {
                        ErosionUtils.Misc.sendMsg(s, "This configuration is a boolean, can be either `true` or `false`.");
                        return;
                    }
                    c.setBoolean(Boolean.parseBoolean(value));
                    newValue = Boolean.toString(c.getBoolean());
                    break;
                }
            }
        }

        if(newValue != null)
        {
            var GRAY = Text.Format(Text.Col.GRAY);
            var DARK_AQUA = Text.Format(Text.Col.DARK_AQUA);
            var GOLD = Text.Format(Text.Col.GOLD);
            ErosionUtils.Misc.sendMsg(s, 
                GRAY + "Value of `" +
                DARK_AQUA + config +
                GRAY + "` successfully changed to: " +
                GOLD + newValue
            );
        }
        else ErosionUtils.Misc.sendMsg(s,"Invalid configuration identifier! View the configuration for an identifier list.");
        return;
    }
}
