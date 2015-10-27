/*
 * A Gradle plugin for the creation of Minecraft mods and MinecraftForge plugins.
 * Copyright (C) 2013 Minecraft Forge
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.minecraftforge.gradle.user;

import static net.minecraftforge.gradle.common.Constants.TASK_GENERATE_SRGS;

import java.util.Collection;
import java.util.List;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.bundling.Jar;

import net.minecraftforge.gradle.util.GradleConfigurationException;
import net.minecraftforge.gradle.util.delayed.DelayedFile;

public class ReobfTaskFactory implements NamedDomainObjectFactory<IReobfuscator>
{
    public static DelayedFile srgSrg;
    public static DelayedFile notchSrg;

    private final UserBasePlugin<?> plugin;

    public ReobfTaskFactory(UserBasePlugin<?> plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public IReobfuscator create(String jarName)
    {
        Task jar = plugin.project.getTasks().getByName(jarName);

        if (!(jar instanceof Jar))
        {
            throw new GradleConfigurationException(jarName + "  is not a jar task. Can only reobf jars!");
        }
        String name = "reobf" + Character.toUpperCase(jarName.charAt(0)) + jarName.substring(1);
        TaskSingleReobf task = plugin.maybeMakeTask(name, TaskSingleReobf.class);

        task.setJar(((Jar) jar).getArchivePath());

        task.dependsOn(TASK_GENERATE_SRGS, jar);
        task.mustRunAfter("test");

        plugin.project.getTasks().getByName("build").dependsOn(task);
        plugin.project.getTasks().getByName("assemble").dependsOn(task);

        plugin.setupReobf(task);

        return new TaskWrapper(jarName, task);
    }

    class TaskWrapper implements IReobfuscator
    {
        private final String name;
        private final IReobfuscator reobf;

        public TaskWrapper(String name, IReobfuscator reobf)
        {
            this.name = name;
            this.reobf = reobf;
        }

        public String getName()
        {
            return name;
        }

        /**
         * Returns the instance of {@link TaskSingleReobf} that this object
         * wraps.
         *
         * @return The task
         */
        public IReobfuscator getTask()
        {
            return reobf;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof TaskWrapper)
            {
                return name.equals(((TaskWrapper) obj).name);
            }
            return false;
        }

        public Object getMappings()
        {
            return reobf.getMappings();
        }

        public void setMappings(Object srg)
        {
            reobf.setMappings(srg);
        }

        public void setClasspath(FileCollection classpath)
        {
            reobf.setClasspath(classpath);
        }

        public FileCollection getClasspath()
        {
            return reobf.getClasspath();
        }

        public List<Object> getExtra()
        {
            return reobf.getExtra();
        }

        public void setExtra(List<Object> extra)
        {
            reobf.setExtra(extra);
        }

        public void extra(Object... o)
        {
            reobf.extra(o);
        }

        public void extra(Collection<Object> o)
        {
            reobf.extra(o);
        }

        public void useSrgSrg()
        {
            reobf.useSrgSrg();
        }

        public void useNotchSrg()
        {
            reobf.useNotchSrg();
        }
    }
}
