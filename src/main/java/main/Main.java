package main;

import Services.CDRGenerationService.CDRGenerationService;
import Services.UDRGeneration.UDRGenerationService;


public class Main {
    public static void main(String[] args){
        CDRGenerationService cdrGenerationService = new CDRGenerationService();
        cdrGenerationService.generate();
        UDRGenerationService udrGenerationService = new UDRGenerationService();
        try {
            if (args.length == 0) {
                udrGenerationService.generateReport();
            } else if (args.length == 1) {
                udrGenerationService.generateReport(args[0]);
            } else if (args.length == 2) {
                udrGenerationService.generateReport(args[0], Integer.parseInt(args[1]));
            } else System.out.println("Too many arguments");
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }


    }
}