package main;

import Services.CDRGenerationService.CDRGenerationService;
import Services.UDRGeneration.UDRGenerationService;


public class Main {
    public static void main(String[] args){
        CDRGenerationService cdrGenerationService = new CDRGenerationService();
        cdrGenerationService.generate();
        UDRGenerationService udrGenerationService = new UDRGenerationService();
        udrGenerationService.generateReport();
    }
}