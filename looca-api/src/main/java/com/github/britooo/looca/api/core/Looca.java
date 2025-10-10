package com.github.britooo.looca.api.core;

import com.github.britooo.looca.api.group.rede.Rede;
import oshi.SystemInfo;

public class Looca {

    private final Rede rede;


    public Looca() {
        SystemInfo si = new SystemInfo();

        this.rede = new Rede(si);
    }
    /**
     * Retorna um <b>Objeto de Rede</b> que contém métodos relacionados a coleta de informações de <b>Rede</b>.
     * @return Objeto Rede.
     */
    public Rede getRede() {
        return rede;
    }

}
