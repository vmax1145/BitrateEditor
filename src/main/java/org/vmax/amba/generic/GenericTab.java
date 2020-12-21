package org.vmax.amba.generic;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public interface GenericTab {
    String getTabLabel();

    ImportAction getImportAction();

    ExportAction getExportAction();

    default List<Action> getOtherActions() {
        return new ArrayList<>();
    }
}
