#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License. See accompanying LICENSE file.
#
import os
import sys
import falcon_confg as fc
import subprocess
cmd = sys.argv[0]
prg, base_dir = fc.resolve_sym_link(os.path.abspath(cmd))

# Use port 16000, if not specified
port_arg = '-port 16000' if '-port' not in sys.argv else ''

other_args = ' '.join(arg for arg in sys.argv[1:])
properties = '-Dfalcon.embeddedmq=false -Dfalcon.domain=prism'
service_start_cmd = os.path.join(base_dir, 'bin', 'service_start.py')
subprocess.call(['python', service_start_cmd, 'prism', port_arg,
                 properties, other_args])
