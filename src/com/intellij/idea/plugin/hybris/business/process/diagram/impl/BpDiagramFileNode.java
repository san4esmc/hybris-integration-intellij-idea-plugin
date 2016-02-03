/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2015 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.business.process.diagram.impl;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.icons.AllIcons;
import com.intellij.idea.plugin.hybris.business.process.diagram.BpDiagramProvider;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


/**
 * Created 11:34 PM 31 January 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class BpDiagramFileNode extends DiagramNodeBase<BpGraphNode> {

    private final BpGraphNode diagramNode;

    public BpDiagramFileNode(final BpGraphNode file) {
        super(ServiceManager.getService(BpDiagramProvider.class));
        this.diagramNode = file;
    }

    @NotNull
    @Override
    public String getTooltip() {
        return this.getIdentifyingElement().getGenericAction().getId();
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @NotNull
    @Override
    public BpGraphNode getIdentifyingElement() {
        return this.diagramNode;
    }
}