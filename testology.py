# Copyright (c) 2015 SONATA-NFV and Paderborn University
# ALL RIGHTS RESERVED.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Neither the name of the SONATA-NFV, Paderborn University
# nor the names of its contributors may be used to endorse or promote
# products derived from this software without specific prior written
# permission.
#
# This work has been performed in the framework of the SONATA project,
# funded by the European Commission under Grant number 671517 through
# the Horizon 2020 and 5G-PPP programmes. The authors would like to
# acknowledge the contributions of their colleagues of the SONATA
# partner consortium (www.sonata-nfv.eu).
import logging
from mininet.log import setLogLevel, info
from emuvim.dcemulator.net import DCNetwork
from emuvim.api.rest.rest_api_endpoint import RestApiEndpoint
from emuvim.api.openstack.openstack_api_endpoint import OpenstackApiEndpoint
from mininet.node import RemoteController

logging.basicConfig(level=logging.INFO)
setLogLevel('info')  # set Mininet loglevel
logging.getLogger('werkzeug').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.base').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.compute').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.keystone').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.nova').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.neutron').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.heat').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.heat.parser').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.glance').setLevel(logging.DEBUG)
logging.getLogger('api.openstack.helper').setLevel(logging.DEBUG)


def create_topology():
    net = DCNetwork(monitor=False, enable_learning=True)

    #c0 = net.addController( 'c0', controller=RemoteController, ip='127.0.0.1', port=8080 )
    #ryu = net.addDocker( 'ryu', ip='127.0.0.1', port=8080, dimage="osrg/ryu" )
    #net.addLink( ryu, c0 )

    info('*** Adding datacenter\n')

    dc1 = net.addDatacenter("dc1")
    # add OpenStack-like APIs to the emulated DC
    api1 = OpenstackApiEndpoint("0.0.0.0", 6001)
    api1.connect_datacenter(dc1)
    api1.start()
    api1.connect_dc_network(net)
    # add the command line interface endpoint to the emulated DC (REST API)
    rapi1 = RestApiEndpoint("0.0.0.0", 5001)
    rapi1.connectDCNetwork(net)
    rapi1.connectDatacenter(dc1)
    rapi1.start()

    info('*** Adding docker containers\n')
    GF1 = net.addDocker('GF1', ip='10.0.0.1', dimage="super:latest", environment={"CONTAINER_NAME":"GF1"}, dcmd="python3 /code/init.py")
    GF2 = net.addDocker('GF2', ip='10.0.0.2', dimage="super:latest", environment={"CONTAINER_NAME":"GF2"}, dcmd="python3 /code/init.py")
    GF3 = net.addDocker('GF3', ip='10.0.0.3', dimage="super:latest", environment={"CONTAINER_NAME":"GF3"}, dcmd="python3 /code/init.py")
    GI = net.addDocker('GI', ip='10.0.0.4', dimage="super:latest", environment={"CONTAINER_NAME":"GI"}, dcmd="python3 /code/init.py")
    server = net.addDocker('server', ip='10.0.0.5', dimage="super:latest", environment={"CONTAINER_NAME":"server"}, dcmd="python3 /code/init.py")

    info('*** Adding switch\n')
    s1 = net.addSwitch('s1')
    s2 = net.addSwitch('s2')
    s3 = net.addSwitch('s3')

    info('*** Creating links\n')
    net.addLink(GF1, s2)
    net.addLink(GF2, s2)
    net.addLink(GF3, s2)
    net.addLink(s1, s2)
    net.addLink(s1, s3)
    net.addLink(GI, s1)
    net.addLink(server, s1)
    net.addLink(dc1, s3)

    net.start()
    net.CLI()
    # when the user types exit in the CLI, we stop the emulator
    net.stop()


def main():
    create_topology()


if __name__ == '__main__':
    main()
