package com.github.qeaml.mmic.fabric;

import com.github.qeaml.mmic.Client;

import net.fabricmc.api.ClientModInitializer;

public class Initializer implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    Client.init();
  }
}
