package com.mandacarubroker.controller;

import com.mandacarubroker.MandacaruBrokerApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = MandacaruBrokerApplication.class)
@ActiveProfiles(value = "test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerIT {

}
