/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.util;

/**
 *
 * @author joao
 */

public class TesteConexao {
    public static void main(String[] args) {
        try {
            System.out.println("Conectando...");
            Database.getConnection();
            System.out.println("Conectado com sucesso!");
            Database.closeConnection();
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }
}
