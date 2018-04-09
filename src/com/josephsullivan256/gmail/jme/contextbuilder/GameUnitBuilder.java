package com.josephsullivan256.gmail.jme.contextbuilder;

import java.util.HashMap;
import java.util.Map;

import com.josephsullivan256.gmail.jme.core.Component;
import com.josephsullivan256.gmail.jme.core.Context;
import com.josephsullivan256.gmail.jme.core.DependencyManager;
import com.josephsullivan256.gmail.jme.core.Entity;
import com.josephsullivan256.gmail.jme.core.EntityCollection;
import com.josephsullivan256.gmail.jme.core.GameUnit;
import com.josephsullivan256.gmail.jme.core.Action;
import com.josephsullivan256.gmail.jme.actions.SuperAction;
import com.josephsullivan256.gmail.jme.util.Nothing;

import com.josephsullivan256.gmail.doxml.Document;
import com.josephsullivan256.gmail.doxml.Element;

public class GameUnitBuilder {
	
	private Map<String,Builder<?>> dependencyBuilders;
	private Map<String,Builder<? extends Action<Nothing>>> actionBuilders;
	
	public GameUnitBuilder(){
		dependencyBuilders = new HashMap<String,Builder<?>>();
		actionBuilders = new HashMap<String,Builder<? extends Action<Nothing>>>();
	}
	
	public void addDependencyBuilder(String name, Builder<?> dependencyBuilder){
		dependencyBuilders.put(name, dependencyBuilder);
	}
	
	public void addActionBuilder(String name, Builder<? extends Action<Nothing>> actionBuilder){
		actionBuilders.put(name, actionBuilder);
	}
	
	private DependencyManager createDependencyManager(Element dependencies){
		DependencyManager dependenciesManager = new DependencyManager();
		
		if(dependencies != null){
			for(Element dep: dependencies.getChildren("dependency")){
				String id = dep.get("id");
				Builder<?> depBuilder = dependencyBuilders.get(id);
				if(depBuilder != null){
					dependenciesManager.addDependency(id,depBuilder.build(dep));
				}
			}
		}
		
		return dependenciesManager;
	}
	
	public GameUnit build(Document d){
		Element root = d.getRoot();
		
		GameUnit unit = new GameUnit(createDependencyManager(root.getChild("dependencies")));
		
		Element contexts = root.getChild("contexts");
		if(contexts != null){
			for(Element ctx: contexts.getChildren("context")){
				Context c = new Context(createDependencyManager(ctx.getChild("dependencies")));
				
				for(Element step: ctx.getChild("actions").getChildren("action")){
					Builder<? extends Action<Nothing>> builder = actionBuilders.get(step.get("id"));
					if(builder != null){
						c.addAction(builder.build(step));
					}
				}
				
				unit.addContext(ctx.get("id"),c);
			}
		}
		
		return unit;
	}
	
	public static class EntityCollectionBuilder implements Builder<EntityCollection>{
		
		private Map<String,Builder<Component>> componentBuilders;
		
		public EntityCollectionBuilder(){
			
			componentBuilders = new HashMap<String,Builder<Component>>();
		}
		
		public void addComponentBuilder(String name, Builder<Component> componentBuilder){
			componentBuilders.put(name, componentBuilder);
		}
		
		@Override
		public EntityCollection build(Element e) {
			
			EntityCollection c = new EntityCollection();
			
			for(Element e_entity: e.getChildren("entity")){
				Entity entity = c.createEntity();
				for(Element e_component: e_entity.getChildren("component")){
					entity.add(componentBuilders.get(e_component.get("id")).build(e_component));
				}
			}
			return c;
		}
		
	}
	
	public static abstract class SuperActionBuilder<A,B> implements Builder<SuperAction<A,B>>{
		private Map<String,Builder<? extends Action<B>>> builders;
		
		public SuperActionBuilder(){
			builders = new HashMap<String,Builder<? extends Action<B>>>();
		}
		public void addActionBuilder(String name, Builder<? extends Action<B>> actionBuilder){
			builders.put(name, actionBuilder);
		}
		
		@Override
		public SuperAction<A,B> build(Element e) {
			SuperAction<A,B> system = buildSuperAction(e);
			for(Element actionElement: e.getChildren("action")){
				Builder<? extends Action<B>> builder = builders.get(actionElement.get("id"));
				if(builder != null) system.addSubaction(builder.build(actionElement));
			}
			return system;
		}
		
		public abstract SuperAction<A,B> buildSuperAction(Element e);
	}
}
