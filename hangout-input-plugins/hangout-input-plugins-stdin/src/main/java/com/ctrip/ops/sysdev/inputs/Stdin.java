package com.ctrip.ops.sysdev.inputs;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("ALL")
public class Stdin extends BaseInput {
    private static final Logger logger = Logger
            .getLogger(Stdin.class.getName());

    private boolean hostname;
    private String hostnameValue;

    public Stdin(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
    }

    @Override
    protected void prepare() {
        this.decoder = this.createDecoder();
        this.filterProcessors = this.createFilterProcessors();
        this.outputProcessors = this.createOutputProcessors();

        if (config.containsKey("hostname")) {
            this.hostname = (Boolean) config.get("hostname");
        } else {
            this.hostname = false;
        }

        if (this.hostname) {
            try {
                this.hostnameValue = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                logger.warn("failed to get hostname");
                this.hostname = false;
            }
        }
    }

    @Override
    protected Map<String, Object> preprocess(Map<String, Object> event) {
        if (this.hostname) {
            event.put("hostname", this.hostnameValue);
        }
        return event;
    }

    public void emit() {
        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));

            String input;

            while ((input = br.readLine()) != null) {
                this.process(input);
            }

        } catch (IOException io) {
            io.printStackTrace();
            logger.error("Stdin loop got exception");
            System.exit(1);
        }
    }
}
