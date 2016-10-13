/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.type.system.validation.impl;

import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.type.system.validation.ItemsFileValidation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ITEMS_XML_FILE;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class ItemsXMLChangedListener implements ProjectManagerListener {


    private SaveItemXmlFileListener saveItemXmlFileListener;

    protected class ItemsFileSelectedListener extends FileEditorManagerAdapter {

        private ItemsFileValidation validator;

        public ItemsFileSelectedListener(final Project project) {
            super();
            this.validator = new DefaultItemsFileValidation(project);
        }

        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            super.fileOpened(source, file);
            this.validator.validateItemFile(file);

        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            super.selectionChanged(event);
            if (null != event.getNewFile()) {
                this.validator.validateItemFile(event.getNewFile());
            }
        }
    }

    protected class SaveItemXmlFileListener extends VirtualFileAdapter {

        private ItemsFileValidation validator;

        public SaveItemXmlFileListener(final Project project) {
            super();
            this.validator = new DefaultItemsFileValidation(project);
        }

        @Override
        public void contentsChanged(VirtualFileEvent event) {
            if (!HybrisApplicationSettingsComponent.getInstance().getState().isValidateGeneratedItemsOnSave()) {
                return;
            }
            super.contentsChanged(event);
            if (!event.getFileName().endsWith(ITEMS_XML_FILE)) {
                return;
            }
            this.validator.validateItemFile(event.getFile());
        }
    }


    @Override
    public void projectOpened(final Project project) {
        final MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            new ItemsFileSelectedListener(project)
        );

        saveItemXmlFileListener = new SaveItemXmlFileListener(project);
        VirtualFileManager.getInstance().addVirtualFileListener(saveItemXmlFileListener);

    }

    @Override
    public boolean canCloseProject(final Project project) {
        return false;
    }

    @Override
    public void projectClosed(final Project project) {
        VirtualFileManager.getInstance().removeVirtualFileListener(saveItemXmlFileListener);
    }

    @Override
    public void projectClosing(final Project project) {

    }
}
