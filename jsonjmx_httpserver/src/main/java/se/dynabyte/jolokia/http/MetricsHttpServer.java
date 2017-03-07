package se.dynabyte.jolokia.http;

import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Logger;
import se.dynabyte.jolokia.exporter.JsonCollector;

import java.io.File;
import java.net.InetSocketAddress;

public class MetricsHttpServer {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: WebServer <[hostname:]port> <yaml configuration file> <metrics json configuration file>");
            System.exit(1);
        }

        String[] hostnamePort = args[0].split(":");
        int port;
        InetSocketAddress socket;

        if (hostnamePort.length == 2) {
            port = Integer.parseInt(hostnamePort[1]);
            socket = new InetSocketAddress(hostnamePort[0], port);
        } else {
            port = Integer.parseInt(hostnamePort[0]);
            socket = new InetSocketAddress(port);
        }

        JsonCollector collector = new JsonCollector(new File(args[1]), new File(args[2])).register();

        Server server = new Server(socket);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
        server.start();
        server.join();
    }
}
