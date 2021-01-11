import logging

class ssdp:
    def __init__(self):
        self.logger = logging.getLogger('sempGateway')
        self.logger.debug("ssdp")
    
    def test(self):
        self.logger.debug("test")