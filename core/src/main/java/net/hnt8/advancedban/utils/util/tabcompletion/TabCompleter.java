package net.hnt8.advancedban.utils.util.tabcompletion;

import java.util.List;

@FunctionalInterface
public interface TabCompleter {
    List<String> onTabComplete(Object user, String[] args);
}
