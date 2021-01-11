import logging
import logging.config
from os import path
import ssdp


def main():
    root_path = path.dirname(path.abspath(__file__))
    logging.config.fileConfig(path.join(root_path, "logger.conf"))

    # create logger
    logger = logging.getLogger('sempGateway')

    logger.info("##############################################################################################")         
    logger.info("###  Starting SEMP Gateway") 
    logger.info("##############################################################################################") 

    test = ssdp.ssdp()
    test.test()

    logger.critical("critical")
    logger.error("error")
    logger.warning("warning")
    logger.debug("debug")
    logger.info("info")         


if __name__ == '__main__': main()
